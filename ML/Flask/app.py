from flask import Flask, request, jsonify
from gensim.models import Word2Vec
import numpy as np
from tensorflow.keras.models import load_model
import pandas as pd
from pydantic import BaseModel
from sklearn.metrics.pairwise import cosine_similarity

app = Flask(__name__)

# Load Word2Vec model
model = Word2Vec.load("word2vec.model")

# Load Neural Network model
neural_network_model = load_model("rec_model.h5")

# Load dataset jobs
data = pd.read_csv("job.csv")

cols = list(['JobID']+['Posisi']+ ['Skill 1']+ ['Skill 2']+ ['Skill 3'])
final_lowongan = data[cols]
final_lowongan.columns = ['Job.ID','Position','Skill1','Skill2','Skill3']

final_lowongan["Skill1"] = final_lowongan["Skill1"].str.replace(' ', '_')
final_lowongan["Skill2"] = final_lowongan["Skill2"].str.replace(' ', '_')
final_lowongan["Skill3"] = final_lowongan["Skill3"].str.replace(' ', '_')

final_lowongan["pos_skill"] = final_lowongan["Skill1"] +", "+ final_lowongan["Skill2"]+", "+ final_lowongan["Skill3"]

from nltk.corpus import stopwords
from nltk.stem import PorterStemmer
import re 

# Create an object of class PorterStemmer
porter = PorterStemmer()

# Define the stop words
stop_words = set(stopwords.words('english')) 

# Function to preprocess the data
def preprocess_data(data):
    # Lowercasing the data
    data = data.lower()
    # Removing the punctuations, except comma
    data = re.sub(r'[^\w\s,]', '', data)
    # Stemming the data
    data = porter.stem(data)
    # Removing the stop words
    data = " ".join(word for word in data.split() if word not in stop_words)
    return data

#  Preprocessing the job descriptions
final_lowongan["pos_skill"] = final_lowongan["pos_skill"].apply(preprocess_data)

data2 = list(final_lowongan["pos_skill"].values)

# Membuat list_of_descriptions
list_of_descriptions = [desc.split(', ') for desc in data2]

# Fungsi untuk mendapatkan vektor dari kata-kata dalam skill_list
def get_vector(word_list):
    vector_sum = sum(model.wv[word] for word in word_list if word in model.wv)
    return vector_sum / len(word_list)
    
job_vectors = np.array([get_vector(desc) for desc in list_of_descriptions])
job_vectors = np.reshape(job_vectors, (len(job_vectors), -1))  # reshape job vectors to 2D

# Fungsi untuk mendapatkan posisi yang mirip dengan skill 1 dan mengutamakan skill pertama
def get_similar_positions(skill_list):
    skill_vector = get_vector(skill_list)
    predicted_vector = neural_network_model.predict(np.reshape(skill_vector, (1, -1)))[0]
    
    skill1_vector = get_vector([skill_list[0]])
    skill1_similarity = cosine_similarity([skill1_vector], job_vectors)[0]
    
    similarities = 0.7 * skill1_similarity + 0.3 * cosine_similarity([predicted_vector], job_vectors)[0]
    
    df = pd.DataFrame(data={
        'JobID': data['JobID'],
        'Position': data['Posisi'],
        'Kualifikasi' : data['Kualifikasi'],
        'Jenjang': data['Jenjang'],
        'Kota' : data['Kota'],
        'Deskripsi' : data['Deskripsi'],
        'Similarity': similarities
    })
    df = df.sort_values(by='Similarity', ascending=False)
    return df.head(10)

# Definisikan struktur payload permintaan
class SkillRequest(BaseModel):
    skill1: str
    skill2: str
    skill3: str
    
@app.route("/")
def hello_world():
    return "<p>Hello, World!</p>"

# Route for recommendation API
@app.route("/recommend-positions", methods=["POST"])
def recommend_positions():
    skill_list = [request.form["skill1"].lower(), request.form["skill2"].lower(), request.form["skill3"].lower()]
    result = get_similar_positions(skill_list)
    return result.to_dict(orient='records')

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)
