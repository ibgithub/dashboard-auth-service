# Dokumen Requirements

## Pendahuluan

Dokumen ini mendefinisikan requirements untuk perbaikan auth-service backend berbasis Spring Boot. Terdapat tiga area perbaikan utama: penanganan error pada login, konfigurasi CORS untuk akses cross-origin, dan perbaikan kerentanan SQL Injection pada method `countAll` di UserRepository.

## Glosarium

- **Auth_Service**: Layanan backend berbasis Spring Boot yang menangani autentikasi dan manajemen pengguna.
- **Auth_Controller**: Controller REST yang menangani endpoint autentikasi (`/api/auth/login`).
- **Auth_Service_Layer**: Service layer yang berisi logika bisnis autentikasi termasuk validasi kredensial.
- **Global_Exception_Handler**: Komponen Spring (`@ControllerAdvice`) yang menangkap exception secara terpusat dan mengonversinya menjadi HTTP response yang konsisten.
- **CORS_Configuration**: Konfigurasi Cross-Origin Resource Sharing yang mengatur origin mana yang diizinkan mengakses API.
- **User_Repository**: Repository yang menangani operasi database untuk entitas pengguna.
- **ApiResponse**: Struktur response JSON standar yang digunakan oleh seluruh endpoint API.
- **Parameterized_Query**: Teknik query database yang memisahkan parameter dari SQL string untuk mencegah SQL Injection.

## Requirements

### Requirement 1: Penanganan Error Login yang Tepat

**User Story:** Sebagai pengguna API, saya ingin menerima HTTP status code dan response body yang sesuai ketika login gagal, sehingga saya dapat menangani error dengan benar di sisi client.

#### Acceptance Criteria

1. WHEN login gagal karena username tidak ditemukan, THEN THE Auth_Service_Layer SHALL melempar custom exception bertipe `AuthenticationException` (bukan `RuntimeException`).
2. WHEN login gagal karena password tidak cocok, THEN THE Auth_Service_Layer SHALL melempar custom exception bertipe `AuthenticationException` (bukan `RuntimeException`).
3. WHEN `AuthenticationException` terjadi pada endpoint login, THEN THE Global_Exception_Handler SHALL mengembalikan HTTP 401 Unauthorized dengan body JSON berformat `ApiResponse` yang berisi field `success: false`, `code: "UNAUTHORIZED"`, `message` yang deskriptif, dan `timestamp`.
4. WHEN exception tidak tertangani terjadi di endpoint manapun, THEN THE Global_Exception_Handler SHALL mengembalikan HTTP 500 Internal Server Error dengan body JSON berformat `ApiResponse` yang berisi field `success: false`, `code: "INTERNAL_ERROR"`, `message` generik, dan `timestamp`.
5. THE Global_Exception_Handler SHALL menangani semua exception menggunakan satu class `@ControllerAdvice` terpusat.

### Requirement 2: Konfigurasi CORS

**User Story:** Sebagai developer frontend, saya ingin API auth-service dapat diakses dari origin frontend yang berbeda, sehingga aplikasi frontend dapat berkomunikasi dengan backend tanpa diblokir oleh browser.

#### Acceptance Criteria

1. THE CORS_Configuration SHALL mengizinkan request dari origin frontend yang dikonfigurasi melalui application properties.
2. THE CORS_Configuration SHALL mengizinkan HTTP methods: GET, POST, PUT, DELETE, dan OPTIONS.
3. THE CORS_Configuration SHALL mengizinkan header: `Authorization`, `Content-Type`, dan `Accept`.
4. THE CORS_Configuration SHALL mengekspos header `Authorization` pada response.
5. THE CORS_Configuration SHALL diterapkan pada seluruh endpoint API (`/api/**`).
6. THE Security_Config SHALL mengintegrasikan CORS configuration ke dalam Spring Security filter chain.

### Requirement 3: Perbaikan Kerentanan SQL Injection pada Method `countAll`

**User Story:** Sebagai security engineer, saya ingin method `countAll` di UserRepository menggunakan parameterized query, sehingga tidak ada celah SQL Injection yang dapat dieksploitasi oleh penyerang.

#### Acceptance Criteria

1. WHEN parameter keyword diberikan pada method `countAll`, THEN THE User_Repository SHALL menggunakan parameterized query (placeholder `?`) untuk memasukkan keyword ke dalam SQL statement.
2. THE User_Repository SHALL menghilangkan seluruh string concatenation langsung dari user input ke dalam SQL query pada method `countAll`.
3. WHEN parameter keyword berisi karakter khusus SQL (misalnya `'`, `"`, `;`, `--`), THEN THE User_Repository SHALL memperlakukannya sebagai data literal tanpa mengeksekusinya sebagai perintah SQL.
4. WHEN parameter keyword bernilai null atau kosong pada method `countAll`, THEN THE User_Repository SHALL mengembalikan total count seluruh user tanpa menerapkan filter.

## Catatan Tambahan

- Semua perbaikan harus backward-compatible dengan endpoint yang sudah ada.
- Struktur `ApiResponse` yang sudah ada di package `com.ib.auth.common` harus digunakan sebagai format response error.
- Konfigurasi CORS origin sebaiknya dapat dikonfigurasi melalui file `application.yaml` agar mudah disesuaikan per environment.
