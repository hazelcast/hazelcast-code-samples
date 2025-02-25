import numpy as np
import pandas as pd
import seaborn as sns
import matplotlib as plt
from sklearn import datasets
from sklearn.cluster import KMeans
from sklearn.utils import shuffle
from sklearn.metrics import confusion_matrix

df1 = pd.read_csv("moviedb/movies.csv")
df2 = pd.read_csv("moviedb/cast.csv")
df3 = pd.read_csv("moviedb/ratings.csv")
df4 = pd.read_csv("moviedb/users.csv")

