import pandas as pd
import json
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity

genres = ['unknown', 'action', 'adventure', 'animation', 'childrens', 'comedy', 'crime', 'documentary', 'drama',
          'fantasy', 'noir', 'horror', 'musical', 'mystery', 'romance', 'scifi', 'thriller', 'war', 'western']


def onehotencoding2genre(x):
    ret_val = []
    for c in genres:
        g = getattr(x, c)
        if g == 1:
            ret_val.append(c)
    return ret_val

# read movies, convert json fields to objects
df_movies = pd.read_csv("moviedb/movies_cast_company.csv", encoding='utf8')
df_movies["cast"] = df_movies["cast"].apply(lambda x: json.loads(x))
df_movies["company"] = df_movies["company"].apply(lambda x: json.loads(x))
df_movies["genres"] = df_movies.apply(lambda x: onehotencoding2genre(x), axis=1)

# read movie ratings
df_ratings = pd.read_csv("moviedb/ratings.csv")

# read users 
df_users = pd.read_csv("moviedb/users.csv")

# merge movies, ratings and users into one big df
df = pd.merge(df_movies, df_ratings, on="movie_id_ml")
df = pd.merge(df, df_users, on="user_id")


# compute the number or rating and mean rating by movie
df_movie_count_mean = df.groupby(["movie_id_ml", "title"], as_index=False)["rating"].agg(
    ["count", "mean"]).reset_index()

# compute the mean rating across all movies
C = df_movie_count_mean["mean"].mean()

# compute the 90th percentile rating across all movies
m = df_movie_count_mean["count"].quantile(0.9)

df_movies_1 = df_movie_count_mean.copy()

# merge in the number of ratings and the average rating
df = pd.merge(df_movies, df_movies_1, on=["movie_id_ml", "title"])


# compute a rating that is normalized in some sense (more details wanted)
def weighted_rating(x, m=m, C=C):
    """Calculation based on the IMDB formula"""
    v = x['count']
    R = x['mean']
    return (v / (v + m) * R) + (m / (m + v) * C)


# Define a new feature 'score' and calculate its value with `weighted_rating()`
df['score'] = df.apply(weighted_rating, axis=1)
# Sort movies based on score calculated above
df = df.sort_values('score', ascending=False).reset_index()

df_cbr = pd.DataFrame()

# handle cast
limit_cast_num = 10
df_cbr['cast'] = df['cast'].apply(
    lambda x: [''.join(i['cast_name'].split(",")[::-1]) for i in x] if isinstance(x, list) else [])
df_cbr['cast'] = df_cbr['cast'].apply(lambda x: x[:limit_cast_num] if len(x) >= limit_cast_num else x)
df_cbr['cast'] = df_cbr['cast'].apply(lambda x: [str.lower(i.replace(" ", "")) for i in x])

# handle genres
df_cbr['genre'] = df['genres']

# handle title
df_cbr['title'] = df['title']
df_cbr['movie_id_ml'] = df['movie_id_ml']

# merge all
df_cbr['mixed'] = df_cbr['cast'] + df_cbr['genre']
df_cbr['mixed'] = df_cbr['mixed'].apply(lambda x: ' '.join(x))

# I think this is a big vector of words in the cast and genre 
count = CountVectorizer(analyzer='word', ngram_range=(1, 2), min_df=0, stop_words='english')
count_matrix = count.fit_transform(df_cbr['mixed'])
count_matrix.todense()

cosine_sim = cosine_similarity(count_matrix, count_matrix)
indices = pd.Series(df_cbr.index, index=df_cbr['title'])
titles = df_cbr['title']
ml_ids = df_cbr['movie_id_ml']


# returns list corresponding to input list
# each list entry has the format "123,{ JSON formatted recommendation }
# sample recommendation JSON is at the bottom of this file
#
def do_recommender(input_list):
    parsed_input = [item.split(",", maxsplit=1) for item in input_list]

    # result looks like [["123","Toy Story"],["456","Die Hard"]]
    rec_result = [ title[0] + ',' + get_recommendations(title[1].lower()).to_json(orient='table') if title[1].lower() in indices else title[0] + ",{}" for title in parsed_input ]
    return rec_result


def get_recs_for_idx(idx):
    similarity_scores = list(enumerate(cosine_sim[idx]))
    similarity_scores = sorted(similarity_scores, key=lambda x: x[1], reverse=True)
    similarity_scores = similarity_scores[1:21]

    movie_indices = [i[0] for i in similarity_scores]
    similar_scores = [i[1] for i in similarity_scores]
    similar_scores = pd.Series(similar_scores, index=movie_indices)
    recommended_titles = titles.iloc[movie_indices].transform(str.title)
    recommended_mlids = ml_ids.iloc[movie_indices]

    df_titles = recommended_titles.to_frame()
    df_ml_ids = recommended_mlids.to_frame()
    df_ml_ids = df_ml_ids.rename_axis('id')
    df_titles = df_titles.rename_axis('id')
    df_scores = similar_scores.to_frame()
    df_scores = df_scores.rename_axis('id')
    final = df_titles.merge(df_scores, left_on='id', right_on='id')
    final = final.merge(df_ml_ids, left_on='id', right_on='id')
    final = final.drop_duplicates('title')
    return final.dropna()


def get_recommendations(title):
    idxs = indices[title]
    if "int64" in str(type(idxs)):
        return get_recs_for_idx(idxs)
    elif "Series" in str(type(idxs)):
        accum = pd.Series(dtype=object)
        for idx in idxs:
            currentSeries = get_recs_for_idx(idx)
            accum = pd.concat([currentSeries, accum])
            accum = accum.drop_duplicates('title')
        accum = accum[accum != title]
        return accum.dropna()
    else:
        raise TypeError("Unrecognized index type (expected int64 or Series)")

# Sample Invocation
for recommendation in do_recommender(["123,No Such Thing", "456,Die Hard"]):
     results = recommendation.split(",", maxsplit=1)
     print(results[0])
     print(json.dumps(json.loads(results[1]), indent=2))


# Sample Output
# 123
# {}
# 456
# {
#     "schema": {
#         "fields": [
#             {
#                 "name": "id",
#                 "type": "integer"
#             },
#             {
#                 "name": "title",
#                 "type": "string"
#             },
#             {
#                 "name": 0,
#                 "type": "number"
#             },
#             {
#                 "name": "movie_id_ml",
#                 "type": "integer"
#             }
#         ],
#         "primaryKey": [
#             "id"
#         ],
#         "pandas_version": "1.4.0"
#     },
#     "data": [
#         {
#             "id": 893,
#             "title": "Street Fighter",
#             "0": 0.1605144708,
#             "movie_id_ml": 1413
#         },
#         {
#             "id": 575,
#             "title": "Demolition Man",
#             "0": 0.1539600718,
#             "movie_id_ml": 578
#         },
#         {
#             "id": 334,
#             "title": "Die Hard 2",
#             "0": 0.1481481481,
#             "movie_id_ml": 226
#         },
#         {
#             "id": 1132,
#             "title": "Turbulence",
#             "0": 0.1481481481,
#             "movie_id_ml": 986
#         },
#         {
#             "id": 248,
#             "title": "The Long Kiss Goodnight",
#             "0": 0.1203858531,
#             "movie_id_ml": 147
#         },
#         {
#             "id": 341,
#             "title": "Executive Decision",
#             "0": 0.1203858531,
#             "movie_id_ml": 685
#         },
#         {
#             "id": 389,
#             "title": "Breakdown",
#             "0": 0.1203858531,
#             "movie_id_ml": 295
#         },
#         {
#             "id": 532,
#             "title": "Eraser",
#             "0": 0.1203858531,
#             "movie_id_ml": 597
#         },
#         {
#             "id": 719,
#             "title": "Mirage",
#             "0": 0.1203858531,
#             "movie_id_ml": 1673
#         },
#         {
#             "id": 823,
#             "title": "Nick Of Time",
#             "0": 0.1203858531,
#             "movie_id_ml": 761
#         },
#         {
#             "id": 843,
#             "title": "The Courtyard",
#             "0": 0.1203858531,
#             "movie_id_ml": 1548
#         },
#         {
#             "id": 1134,
#             "title": "Natural Born Killers",
#             "0": 0.1203858531,
#             "movie_id_ml": 53
#         },
#         {
#             "id": 1226,
#             "title": "Dante'S Peak",
#             "0": 0.1203858531,
#             "movie_id_ml": 323
#         },
#         {
#             "id": 278,
#             "title": "Diva",
#             "0": 0.120222618,
#             "movie_id_ml": 855
#         },
#         {
#             "id": 80,
#             "title": "The Hunt For Red October",
#             "0": 0.1154700538,
#             "movie_id_ml": 265
#         },
#         {
#             "id": 113,
#             "title": "Air Force One",
#             "0": 0.1154700538,
#             "movie_id_ml": 300
#         },
#         {
#             "id": 396,
#             "title": "Die Hard: With A Vengeance",
#             "0": 0.1154700538,
#             "movie_id_ml": 550
#         },
#         {
#             "id": 582,
#             "title": "The Jackal",
#             "0": 0.1154700538,
#             "movie_id_ml": 689
#         },
#         {
#             "id": 957,
#             "title": "Chasers",
#             "0": 0.1154700538,
#             "movie_id_ml": 1489
#         },
#         {
#             "id": 986,
#             "title": "Hard Rain",
#             "0": 0.1154700538,
#             "movie_id_ml": 349
#         }
#     ]
# }
