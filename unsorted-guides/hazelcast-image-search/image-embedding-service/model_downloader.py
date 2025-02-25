import sentence_transformers

print('downloading model ...')
encoder = sentence_transformers.SentenceTransformer('clip-ViT-B-32')
print('done. saving ...')
encoder.save('../models/clip-ViT-B-32')
print('done')
