from flask import Flask, request, jsonify
import mysql.connector

app = Flask(__name__)

# Membuat koneksi ke database
db_connection = mysql.connector.connect(
    host="host",
    user="user",
    password="password",
    database="database"
)

@app.route('/Register', methods=['POST'])def register():
    # Menerima data dari permintaan POST
    data = request.get_json()

    # Mendapatkan nilai username, email, dan password dari data
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')

    # Lakukan validasi data
    if not username or not email or not password:
        return jsonify({'error': 'Mohon isi semua field'}), 400

    # Membuat kursor untuk melakukan operasi SQL
    cursor = db_connection.cursor()

    # Menjalankan query untuk menyimpan data
    query = "INSERT INTO Users (username, email, password) VALUES (%s, %s, %s)"
    values = (username, email, password)
    cursor.execute(query, values)

    db_connection.commit()

    # Menutup kursor
    cursor.close()

    # Berikan respons sukses
    return jsonify({'message': 'Registrasi berhasil', 'username': username, 'email': email}), 200

@app.route('/Login', methods=['POST'])
def login():
    # Menerima data dari permintaan POST
    data = request.get_json()

    # Mendapatkan nilai email dan password dari data
    email = data.get('email')
    password = data.get('password')

    # Lakukan validasi data
    if not email or not password:
        return jsonify({'error': 'Mohon isi semua field'}), 400

    # Membuat kursor untuk melakukan operasi SQL
    cursor = db_connection.cursor()

    # Menjalankan query untuk mengambil data pengguna dengan email dan password yang sesuai
    query = "SELECT * FROM Users WHERE email = %s AND password = %s"
    values = (email, password)
    cursor.execute(query, values)

    # Mengambil satu baris hasil query
    users = cursor.fetchall()

    # Menutup kursor
    cursor.close()

    # Memeriksa apakah pengguna ditemukan atau tidak
    if users:
        user = users[0]
        # Jika ditemukan, kirim respons sukses
        response = {
            'message': 'Login berhasil',
            'username': user[1],
            'email': user[2]
        }
        return jsonify(response), 200
    else:
        # Jika tidak ditemukan, kirim respons error
        return jsonify({'error': 'Email atau password salah'}), 401

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000)
