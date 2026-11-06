# Ruang Tenang

Aplikasi Android untuk mencatat perasaan harian dalam bentuk jurnal, melihat riwayat via kalender, dan memantau statistik mood.

## Tim

| Nama | NIM |
| :--- | :--- |
| I Kadek Bintang Adi Bimantara | 2405551049 |
| Trio Suro Wibowo | 2405551168 |
| Richard Christian Mozart Diazoni | 2405551019 |
| I Komang Cahya Kertha Yasa | 2405551034 |

## Tech Stack

- **Bahasa:** Kotlin
- **Arsitektur:** MVVM (ViewModel + Repository + Room DAO)
- **Database:** Room (SQLite)
- **Async:** Kotlin Coroutines & LiveData
- **UI:** XML Layouts, Material Components, ConstraintLayout

## Fitur

- Login, registrasi, dan mode Guest
- Jurnal harian dengan 9 tag mood (Senang, Tenang, Netral, Sedih, Cemas, Marah, Malu, Takut, Malas)
- Pencarian entri berdasarkan judul atau isi
- Kalender riwayat dengan navigasi bulanan
- Statistik distribusi mood 30 hari terakhir
- Pencatatan hanya untuk hari ini (menjaga keaslian refleksi)

## Alur Layar

```
Splash → Login/Register/Guest → Beranda
         ├── Tambah/Edit Diary
         ├── Kalender Riwayat
         └── Profil & Statistik
```

## Setup

1. Clone dan buka di Android Studio:
   ```bash
   git clone https://github.com/dekbintang/Ruang-Tenang.git
   ```
2. Tunggu Gradle Sync selesai.
3. Jalankan di emulator atau perangkat fisik (API 24+).

## Lisensi

[MIT](LICENSE)
