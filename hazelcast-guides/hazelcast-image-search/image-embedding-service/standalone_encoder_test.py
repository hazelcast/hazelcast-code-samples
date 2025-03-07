import base64
import json
import clip_image_encoder
import time
from scipy.spatial.distance import cosine
import sentence_transformers

if __name__ == '__main__':
    t0 = time.time()
    test_images = ['http://localhost:8888/dragonfly_0003.jpg',
                   'http://localhost:8888/dragonfly_0002.jpg']

    embeddings = clip_image_encoder.transform_list(test_images)

    outputs = [json.loads(r) for r in embeddings]

    floatarrays = [j['vector'] for j in outputs]
    v1 = floatarrays[0]

    text_encoder = sentence_transformers.SentenceTransformer('clip-ViT-B-32')

    comparisons = ['dragonfly', 'dragon', 'insect', 'wings', 'helicopter', 'bicycle','happy child', 'clouds', 'dragonfly and clouds', 'blue']
    for thing in comparisons:
        v_thing = text_encoder.encode(thing)
        d = cosine(v1, v_thing)
        print(f'distance from "{thing}": {d}')

