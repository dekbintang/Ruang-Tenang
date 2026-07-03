<div align="center">

  # 🍃 Ruang Tenang
  **Ruang Tenang: Digital Diary untuk Mencatat Kegiatan, Emosi dan Pengalaman Harian.**

  <p align="center">
    Aplikasi Android sederhana untuk mencatat perasaan harian dalam bentuk jurnal/diary,
    melihat riwayatnya lewat kalender, dan memantau statistik mood dari waktu ke waktu.
  </p>

</div>

---

## 👥 Tim Pengembang

Proyek ini dikembangkan oleh:

| Nama | NIM |
| :--- | :--- |
| **Trio Suro Wibowo** | `2405551168` |
| **I Kadek Bintang Adi Bimantara** | `2405551049` |
| **Richard Christian Mozart Diazoni** | `2405551019` |
| **I Komang Cahya Kertha Yasa** | `2405551034` |

---

## ✨ Fitur Utama

- 🔐 **Autentikasi**: Login, registrasi akun baru, dan opsi masuk sebagai *Guest*.
- 📖 **Jurnal Perasaan (CRUD)**: Tulis, lihat detail, edit, dan hapus catatan diary harian.
  Setiap entri diary punya judul, isi cerita, dan tag mood (9 pilihan: Senang, Tenang,
  Netral, Sedih, Cemas, Marah, Malu, Takut, Malas). Data tersimpan lokal lewat **Room Database**.
- 🔍 **Pencarian Diary**: Cari entri diary berdasarkan judul atau isi lewat search bar di beranda.
- 📅 **Kalender Riwayat**: Lihat tanggal-tanggal yang punya catatan diary dalam satu bulan,
  navigasi antar bulan, dan buka entri diary dari tanggal yang dipilih. Diary baru hanya
  bisa ditambahkan untuk tanggal hari ini (tidak bisa untuk tanggal lampau/masa depan),
  supaya waktu pencatatan tetap jujur mencerminkan kapan diary itu ditulis.
- 👤 **Profil & Statistik Mood**: Ubah nama/umur pengguna, serta lihat ringkasan
  distribusi mood dari entri diary 30 hari terakhir.

---

## 🛠️ Teknologi yang Digunakan

- **Bahasa Pemrograman**: Kotlin
- **Arsitektur**: MVVM (ViewModel + Repository + Room DAO)
- **Database Lokal**: Room Database (SQLite)
- **Tampilan Data**: RecyclerView dengan `ListAdapter` + `DiffUtil`
- **Asynchronous Programming**: Kotlin Coroutines & LiveData
- **UI**: View-based layout (XML) dengan AppCompat & Material Components,
  navigasi antar layar memakai `Intent` (tanpa Navigation Component graph)

---

## 📱 Struktur Navigasi Aplikasi

1. **Splash Screen** → mengecek sesi login, lalu arahkan ke Login atau Beranda.
2. **Login / Register**: Masuk dengan akun terdaftar, buat akun baru, atau lanjut sebagai Guest.
3. **Beranda (Main)**: Daftar seluruh diary (terbaru di atas), search bar, tombol
   "Tambah Diary" dan "Kalender".
4. **Tambah / Edit Diary**: Form judul, isi cerita, dan pilihan mood emoji.
5. **Detail Diary**: Tampilan lengkap satu entri diary, dengan tombol Edit dan Hapus.
6. **Kalender**: Navigasi bulan, tanggal bertanda punya entri, daftar diary per tanggal.
7. **Profil**: Info pengguna (nama, umur), statistik mood, dan tombol Logout.

---

## 🚀 Cara Menjalankan Proyek

1. **Clone repositori ini** ke mesin lokal Anda:
   ```bash
   git clone https://github.com/username/RuangTenang.git
   ```
2. Buka proyek menggunakan **Android Studio** (disarankan versi terbaru).
3. Tunggu hingga proses **Gradle Sync** selesai.
4. Pastikan Anda memiliki emulator (AVD) yang berjalan atau perangkat fisik Android
   yang terhubung (dengan *USB Debugging* aktif).
5. Klik tombol **Run** (▶️) di Android Studio atau tekan `Shift + F10`.

---

<div align="center">
  <i>Untuk kesehatan mental yang lebih baik.</i>
</div>
