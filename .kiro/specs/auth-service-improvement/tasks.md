# Implementation Plan: Auth Service Improvement

## Overview

Rencana implementasi untuk tiga perbaikan pada auth-service: penanganan error login dengan custom exception dan global exception handler, konfigurasi CORS terintegrasi dengan Spring Security, dan perbaikan kerentanan SQL Injection pada `UserRepository.countAll()`. Semua perubahan backward-compatible dengan API yang sudah ada.

## Tasks

- [x] 1. Modifikasi `ApiResponse` dan buat custom exception
  - [x] 1.1 Verifikasi class `ApiResponse` sudah mendukung format error response
    - Pastikan constructor `ApiResponse(boolean success, String code, String message, T data)` sudah ada (sudah tersedia)
    - Pastikan field `timestamp` di-set otomatis pada konstruksi
    - _Requirements: 1.3, 1.4_

  - [x] 1.2 Buat class `AuthenticationException` di package `com.ib.auth.exception`
    - Buat file `src/main/java/com/ib/auth/exception/AuthenticationException.java`
    - Class extends `RuntimeException` dengan constructor yang menerima `String message`
    - _Requirements: 1.1, 1.2_

- [x] 2. Buat `GlobalExceptionHandler` dan modifikasi `AuthService`
  - [x] 2.1 Buat class `GlobalExceptionHandler` di package `com.ib.auth.exception`
    - Buat file `src/main/java/com/ib/auth/exception/GlobalExceptionHandler.java`
    - Annotasi dengan `@RestControllerAdvice`
    - Tambahkan handler `handleAuthenticationException` → return HTTP 401 dengan `ApiResponse(false, "UNAUTHORIZED", ex.getMessage(), null)`
    - Tambahkan handler `handleGenericException` → return HTTP 500 dengan `ApiResponse(false, "INTERNAL_ERROR", "Terjadi kesalahan internal", null)`
    - _Requirements: 1.3, 1.4, 1.5_

  - [x] 2.2 Modifikasi `AuthService.login()` untuk menggunakan `AuthenticationException`
    - Ubah `throw new RuntimeException("Invalid username or password")` menjadi `throw new AuthenticationException("Username atau password salah")`
    - Tambahkan import `com.ib.auth.exception.AuthenticationException`
    - _Requirements: 1.1, 1.2_

- [x] 3. Tambahkan konfigurasi CORS
  - [x] 3.1 Tambahkan property `cors.allowed-origins` di `application.yaml`
    - Tambahkan `cors.allowed-origins: http://localhost:3000,http://localhost:5173`
    - _Requirements: 2.1_

  - [x] 3.2 Modifikasi `SecurityConfig` untuk menambahkan CORS configuration
    - Inject `@Value("${cors.allowed-origins}")` untuk membaca allowed origins
    - Buat bean `CorsConfigurationSource` dengan allowed origins, methods (GET, POST, PUT, DELETE, OPTIONS), headers (Authorization, Content-Type, Accept), exposed headers (Authorization), dan `allowCredentials=true`
    - Terapkan pada path `/api/**`
    - Integrasikan di filter chain: `.cors(cors -> cors.configurationSource(corsConfigurationSource()))`
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

- [x] 4. Perbaiki SQL Injection pada `UserRepository.countAll()`
  - [x] 4.1 Refactor method `countAll()` di `UserRepository`
    - Ganti string concatenation `CONCAT('%" + keyword + "%')` dengan parameterized query `CONCAT('%', ?, '%')`
    - Gunakan `jdbcTemplate.queryForObject(sqlSelectCount, Integer.class, keyword, keyword, keyword, keyword)` untuk meneruskan parameter secara aman
    - Pastikan pola konsisten dengan method `findAll(int limit, int offset, String keyword)` yang sudah benar
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 5. Checkpoint - Verifikasi kompilasi dan fungsionalitas dasar
  - Ensure all tests pass, ask the user if questions arise.
  - Pastikan project dapat di-compile tanpa error (`mvn compile`)
  - Verifikasi tidak ada breaking changes pada endpoint yang sudah ada

- [x] 6. Tulis unit tests dan property-based tests
  - [x] 6.1 Tambahkan dependency jqwik di `pom.xml`
    - Tambahkan `net.jqwik:jqwik:1.9.2` dengan scope `test`
    - _Requirements: Testing Strategy_

  - [ ]* 6.2 Tulis unit tests untuk `GlobalExceptionHandler`
    - Test `handleAuthenticationException` → verifikasi HTTP 401, body berisi `success=false`, `code="UNAUTHORIZED"`, message sesuai
    - Test `handleGenericException` → verifikasi HTTP 500, body berisi `success=false`, `code="INTERNAL_ERROR"`, message generik
    - Buat file `src/test/java/com/ib/auth/exception/GlobalExceptionHandlerTest.java`
    - _Requirements: 1.3, 1.4_

  - [ ]* 6.3 Tulis unit tests untuk `AuthService.login()` error handling
    - Test login dengan username yang tidak ada → throws `AuthenticationException`
    - Test login dengan password salah → throws `AuthenticationException`
    - Test login berhasil → return JWT token
    - Buat file `src/test/java/com/ib/auth/service/AuthServiceTest.java`
    - _Requirements: 1.1, 1.2_

  - [ ]* 6.4 Tulis property-based test: Exception handler menghasilkan response 401 yang konsisten
    - **Property 1: Exception handler menghasilkan response 401 yang konsisten untuk AuthenticationException**
    - Generate random string messages → buat `AuthenticationException` → panggil handler → verifikasi response selalu memiliki `success=false`, `code="UNAUTHORIZED"`, `message` non-null, `timestamp` non-null, dan HTTP status 401
    - Minimum 100 iterasi
    - Tag: `Feature: auth-service-improvement, Property 1: Exception handler menghasilkan response 401 yang konsisten`
    - Buat file `src/test/java/com/ib/auth/exception/GlobalExceptionHandlerPropertyTest.java`
    - **Validates: Requirements 1.3**

  - [ ]* 6.5 Tulis property-based test: Exception handler menghasilkan response 500 yang konsisten
    - **Property 2: Exception handler menghasilkan response 500 yang konsisten untuk exception tidak tertangani**
    - Generate random Exception instances (tipe dan message bervariasi) → panggil handler → verifikasi response selalu memiliki `success=false`, `code="INTERNAL_ERROR"`, message generik (bukan message asli exception), `timestamp` non-null, dan HTTP status 500
    - Minimum 100 iterasi
    - Tag: `Feature: auth-service-improvement, Property 2: Exception handler menghasilkan response 500 yang konsisten`
    - **Validates: Requirements 1.4**

  - [ ]* 6.6 Tulis property-based test: SQL Injection safety pada countAll
    - **Property 3: SQL Injection safety — countAll memperlakukan semua keyword sebagai data literal**
    - Generate random strings termasuk SQL injection payloads (`'`, `"`, `;`, `--`, `OR 1=1`, dll.) → panggil `countAll()` dengan mocked `JdbcTemplate` → verifikasi query menggunakan placeholder `?` dan keyword diteruskan sebagai parameter terpisah
    - Minimum 100 iterasi
    - Tag: `Feature: auth-service-improvement, Property 3: SQL Injection safety`
    - Buat file `src/test/java/com/ib/auth/repository/UserRepositoryPropertyTest.java`
    - **Validates: Requirements 3.1, 3.3**

- [x] 7. Final checkpoint - Pastikan semua test lolos
  - Ensure all tests pass, ask the user if questions arise.
  - Jalankan `mvn test` untuk memastikan semua unit test dan property test berhasil
  - Verifikasi tidak ada regresi pada fungsionalitas yang sudah ada

## Task Dependency Graph

```json
{
  "waves": [
    { "tasks": ["1.1", "1.2"] },
    { "tasks": ["2.1", "2.2"] },
    { "tasks": ["3.1", "3.2"] },
    { "tasks": ["4.1"] },
    { "tasks": ["5"] },
    { "tasks": ["6.1"] },
    { "tasks": ["6.2", "6.3", "6.4", "6.5", "6.6"] },
    { "tasks": ["7"] }
  ]
}
```

## Notes

- Task yang ditandai `*` bersifat opsional dan dapat dilewati untuk MVP lebih cepat
- Setiap task merujuk ke requirements spesifik untuk traceability
- Checkpoint memastikan validasi bertahap
- Property tests memvalidasi properti universal kebenaran (correctness)
- Unit tests memvalidasi contoh spesifik dan edge case
- Library property-based testing: **jqwik** (untuk JUnit 5 di Java)
