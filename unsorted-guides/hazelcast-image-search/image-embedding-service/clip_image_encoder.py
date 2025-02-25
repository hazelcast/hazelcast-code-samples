import io
import json

import PIL.Image
import requests
import sentence_transformers

encoder = sentence_transformers.SentenceTransformer('/project/models/clip-ViT-B-32')
print("downloaded and initialized encoder")


#
# The input is a list of URLS of images to retrieve and embed
#
# The output will be a list in the following form:
#   {"metadata": any-json-content, "vector", [json, encoded, list, of, numbers]}
#
# Note: metadata is not used, it is just passed through
#
def transform_list(image_urls: list[str]) -> list[str]:
    results = []

    # could use list comprehensions throughout but not doing it because I need
    # to capture exceptions on individual items
    for url in image_urls:
        try:
            r = requests.get(url)
            if r.status_code != 200:
                results.append({'exception': f'GET {url} returned status code {r.status_code}'})
                continue  # CONTINUE

            # load image from byte streams
            byte_stream = io.BytesIO(r.content)
            image = PIL.Image.open(byte_stream)

            # perform encoding - returns numpy array with the embedding for this image
            embedding = encoder.encode(image)

            # close byte streams
            byte_stream.close()

            # Create a float arrays from it
            floatarray = embedding.tolist()
            results.append({'metadata': url, 'vector': floatarray})
        except Exception as x:
            results.append({'exception': str(x)})

    # encode each float array as json and return a list of strings
    return [json.dumps(result) for result in results]
