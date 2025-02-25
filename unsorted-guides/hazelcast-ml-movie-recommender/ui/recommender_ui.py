import os
import sys
import flask
import requests

app = flask.Flask(__name__)

RECOMMENDER_SERVICE_URL_PARAM = 'RECOMMENDER_SERVICE_URL'
if RECOMMENDER_SERVICE_URL_PARAM not in os.environ:
    sys.exit('Please set the ' + RECOMMENDER_SERVICE_URL_PARAM + " environment variable")

recommender_service_url = os.environ[RECOMMENDER_SERVICE_URL_PARAM]


def dummy_response():
    return [{"title": "GoldenEye",
             "image_url": "https://images-na.ssl-images-amazon.com/images/M/MV5BMzk2OTg4MTk1NF5BMl5BanBnXkFtZTcwNjExNTgzNA@@..jpg"},
            {"title": "Desperado",
             "image_url": "https://images-na.ssl-images-amazon.com/images/M/MV5BYjA0NDMyYTgtMDgxOC00NGE0LWJkOTQtNDRjMjEzZmU0ZTQ3XkEyXkFqcGdeQXVyMTQxNzMzNDI@..jpg"},
            {"title": "Four Rooms",
             "image_url": "https://images-na.ssl-images-amazon.com/images/M/MV5BNDc3Y2YwMjUtYzlkMi00MTljLTg1ZGMtYzUwODljZTI1OTZjXkEyXkFqcGdeQXVyMTQxNzMzNDI@..jpg"},
            {"title": "Mad Love",
             "image_url": "https://images-na.ssl-images-amazon.com/images/M/MV5BNDE0NTQ1NjQzM15BMl5BanBnXkFtZTYwNDI4MDU5..jpg"},
            {"title": "The Aristocats",
             "image_url": "https://images-na.ssl-images-amazon.com/images/M/MV5BMTU1MzM0MjcxMF5BMl5BanBnXkFtZTgwODQ0MzcxMTE@..jpg"},
            {"title": "Life of Brian",
             "image_url": "https://images-na.ssl-images-amazon.com/images/M/MV5BMzAwNjU1OTktYjY3Mi00NDY5LWFlZWUtZjhjNGE0OTkwZDkwXkEyXkFqcGdeQXVyMTQxNzMzNDI@..jpg"}]


def normalize_title(title):
    words = title.split()
    return ' '.join(words)


@app.route("/movies", methods=['GET', 'POST'])
def main_page():
    if flask.request.method == 'POST':
        if 'movie-input' in flask.request.form:
            like = flask.request.form['movie-input']
            request_params = {"like": normalize_title(like)}
            response = requests.get(recommender_service_url, params=request_params)
            try:
                recommendations = response.json()
            except requests.exceptions.JSONDecodeError:
                print(r'Could not decode response {response}', file=sys.stderr)
                recommendations = []
        else:
            recommendations = []
    else:
        recommendations = []

    return flask.render_template("recommendations.html", recommendations=recommendations)
