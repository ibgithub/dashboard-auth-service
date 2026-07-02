# Dokumen Requirements: Role Management (RBAC)

## Pendahuluan

Fitur Role Management mengimplementasikan Role-Based Access Control (RBAC) pada auth-service. Saat ini setiap user hanya memiliki satu role yang disimpan sebagai kolom VARCHAR di tabel `auth.users`. Fitur ini mengubah struktur menjadi relasi many-to-many antara user dan role, serta menambahkan manajemen permission berbasis halaman/fitur yang dihubungkan ke role melalui junction table. Admin dapat mengelola role dan permission melalui halaman Role Management, dan sistem akan menggunakan data permission untuk mengontrol akses di backend maupun frontend.

## Glossary

- **Auth_Service**: Microservice Spring Boot yang menangani autentikasi dan otorisasi pengguna
- **Role**: Kumpulan hak akses yang dapat diberikan kepada user (contoh: ADMIN, TREASURY, MARKETING)
- **Permission**: Representasi halaman atau fitur yang dapat diakses (contoh: user_management, role_management, treasury_dashboard)
- **User_Role**: Relasi many-to-many antara user dan role melalui junction table `auth.user_roles`
- **Role_Permission**: Relasi many-to-many antara role dan permission melalui junction table `auth.role_permissions`
- **JWT_Token**: JSON Web Token yang dihasilkan saat login, berisi informasi user termasuk daftar role
- **Admin**: User dengan role ADMIN yang berwenang mengelola role dan permission
- **SecurityFilter**: Filter Spring Security yang memvalidasi JWT token dan memuat authority/permission user
- **Migration_Script**: DDL script untuk membuat tabel baru dan memindahkan data role lama ke struktur baru

## Requirements

### Requirement 1: Pembuatan Tabel Role

**User Story:** Sebagai Admin, saya ingin menyimpan data role dalam tabel terpisah, sehingga role dapat dikelola secara independen dari tabel user.

#### Acceptance Criteria

1. THE Auth_Service SHALL menyediakan tabel `auth.roles` dengan kolom: id (serial primary key), role_name (varchar 50, unique, not null), description (varchar 255, nullable), created_by (varchar 20, not null), created_at (timestamp, default now), updated_by (varchar 20, nullable), updated_at (timestamp, nullable)
2. THE Auth_Service SHALL memastikan kolom role_name pada tabel `auth.roles` bersifat unique dan case-insensitive

### Requirement 2: Pembuatan Tabel Permission

**User Story:** Sebagai Admin, saya ingin menyimpan data permission (halaman/fitur) dalam tabel terpisah, sehingga permission dapat dikelola secara fleksibel.

#### Acceptance Criteria

1. THE Auth_Service SHALL menyediakan tabel `auth.permissions` dengan kolom: id (serial primary key), permission_name (varchar 100, unique, not null), description (varchar 255, nullable), created_by (varchar 20, not null), created_at (timestamp, default now), updated_by (varchar 20, nullable), updated_at (timestamp, nullable)
2. THE Auth_Service SHALL memastikan kolom permission_name pada tabel `auth.permissions` bersifat unique dan case-insensitive

### Requirement 3: Relasi Many-to-Many User dan Role

**User Story:** Sebagai Admin, saya ingin memberikan beberapa role kepada satu user, sehingga user dapat memiliki kombinasi hak akses yang sesuai.

#### Acceptance Criteria

1. THE Auth_Service SHALL menyediakan tabel `auth.user_roles` dengan kolom: user_id (integer, foreign key ke auth.users.id), role_id (integer, foreign key ke auth.roles.id), created_by (varchar 20, not null), created_at (timestamp, default now)
2. THE Auth_Service SHALL memastikan kombinasi user_id dan role_id pada tabel `auth.user_roles` bersifat unique (composite primary key)
3. WHEN sebuah user dihapus, THE Auth_Service SHALL menghapus semua record terkait user tersebut dari tabel `auth.user_roles` (ON DELETE CASCADE)
4. WHEN sebuah role dihapus, THE Auth_Service SHALL menghapus semua record terkait role tersebut dari tabel `auth.user_roles` (ON DELETE CASCADE)

### Requirement 4: Relasi Many-to-Many Role dan Permission

**User Story:** Sebagai Admin, saya ingin menghubungkan beberapa permission ke satu role, sehingga saya dapat mengatur halaman/fitur yang dapat diakses oleh setiap role.

#### Acceptance Criteria

1. THE Auth_Service SHALL menyediakan tabel `auth.role_permissions` dengan kolom: role_id (integer, foreign key ke auth.roles.id), permission_id (integer, foreign key ke auth.permissions.id), created_by (varchar 20, not null), created_at (timestamp, default now)
2. THE Auth_Service SHALL memastikan kombinasi role_id dan permission_id pada tabel `auth.role_permissions` bersifat unique (composite primary key)
3. WHEN sebuah role dihapus, THE Auth_Service SHALL menghapus semua record terkait role tersebut dari tabel `auth.role_permissions` (ON DELETE CASCADE)
4. WHEN sebuah permission dihapus, THE Auth_Service SHALL menghapus semua record terkait permission tersebut dari tabel `auth.role_permissions` (ON DELETE CASCADE)

### Requirement 5: CRUD Role

**User Story:** Sebagai Admin, saya ingin membuat, melihat, mengubah, dan menghapus role, sehingga saya dapat mengelola role yang tersedia di sistem.

#### Acceptance Criteria

1. WHEN Admin mengirim request GET `/api/roles`, THE Auth_Service SHALL mengembalikan daftar semua role beserta deskripsinya
2. WHEN Admin mengirim request GET `/api/roles/{id}`, THE Auth_Service SHALL mengembalikan detail role beserta daftar permission yang terhubung
3. WHEN Admin mengirim request POST `/api/roles` dengan body berisi role_name dan description, THE Auth_Service SHALL membuat role baru dan mengembalikan data role yang dibuat
4. WHEN Admin mengirim request PUT `/api/roles/{id}` dengan body berisi role_name dan description, THE Auth_Service SHALL mengubah data role yang bersangkutan
5. WHEN Admin mengirim request DELETE `/api/roles/{id}`, THE Auth_Service SHALL menghapus role beserta relasi user_roles dan role_permissions yang terkait
6. IF role_name yang dikirim sudah ada di database, THEN THE Auth_Service SHALL mengembalikan error dengan pesan "Role sudah ada"
7. IF role yang akan dihapus masih memiliki user yang terhubung, THEN THE Auth_Service SHALL mengembalikan error dengan pesan "Role masih digunakan oleh user"

### Requirement 6: Assign dan Remove Permission ke Role

**User Story:** Sebagai Admin, saya ingin menambahkan dan menghapus permission dari sebuah role melalui halaman Role Management, sehingga saya dapat mengatur hak akses setiap role.

#### Acceptance Criteria

1. WHEN Admin mengirim request PUT `/api/roles/{id}/permissions` dengan body berisi daftar permission_id, THE Auth_Service SHALL mengganti seluruh permission role tersebut sesuai daftar yang dikirim (replace strategy)
2. WHEN Admin mengirim daftar permission_id kosong, THE Auth_Service SHALL menghapus semua permission dari role tersebut
3. IF salah satu permission_id tidak ditemukan di database, THEN THE Auth_Service SHALL mengembalikan error dengan pesan "Permission tidak ditemukan"
4. WHEN request berhasil, THE Auth_Service SHALL mengembalikan daftar permission terbaru yang terhubung ke role tersebut

### Requirement 7: Assign dan Remove Role ke User

**User Story:** Sebagai Admin, saya ingin memberikan dan mencabut role dari user, sehingga saya dapat mengatur hak akses setiap user.

#### Acceptance Criteria

1. WHEN Admin mengirim request PUT `/api/users/{id}/roles` dengan body berisi daftar role_id, THE Auth_Service SHALL mengganti seluruh role user tersebut sesuai daftar yang dikirim (replace strategy)
2. WHEN Admin mengirim daftar role_id kosong, THE Auth_Service SHALL menghapus semua role dari user tersebut
3. IF salah satu role_id tidak ditemukan di database, THEN THE Auth_Service SHALL mengembalikan error dengan pesan "Role tidak ditemukan"
4. WHEN request berhasil, THE Auth_Service SHALL mengembalikan daftar role terbaru yang terhubung ke user tersebut

### Requirement 8: Ambil Permission User untuk Rendering Menu Frontend

**User Story:** Sebagai Frontend, saya ingin mendapatkan daftar permission user yang sedang login, sehingga saya dapat menampilkan menu sesuai hak akses.

#### Acceptance Criteria

1. WHEN user yang telah login mengirim request GET `/api/users/me/permissions`, THE Auth_Service SHALL mengembalikan daftar semua permission yang dimiliki user berdasarkan role-role yang terhubung
2. THE Auth_Service SHALL menggabungkan permission dari semua role user tanpa duplikasi (distinct)
3. THE Auth_Service SHALL mengembalikan daftar permission dalam format list berisi object dengan field: permission_id, permission_name, dan description

### Requirement 9: Update JWT Token untuk Menyertakan Daftar Role

**User Story:** Sebagai sistem, saya ingin menyertakan daftar role user di JWT token saat login, sehingga backend dan frontend dapat mengetahui role user tanpa query tambahan.

#### Acceptance Criteria

1. WHEN user berhasil login, THE Auth_Service SHALL menyertakan claim "roles" di JWT token berisi daftar role_name yang dimiliki user (format: JSON array of strings)
2. THE Auth_Service SHALL tetap menyertakan claim "role" (singular) berisi role pertama untuk backward compatibility selama masa transisi
3. IF user tidak memiliki role di tabel `auth.user_roles`, THE Auth_Service SHALL menggunakan nilai kolom `role` dari tabel `auth.users` sebagai fallback

### Requirement 10: Update SecurityFilter untuk Permission-Based Authorization

**User Story:** Sebagai sistem, saya ingin memvalidasi akses endpoint berdasarkan permission user, sehingga hanya user dengan permission yang sesuai yang dapat mengakses endpoint tertentu.

#### Acceptance Criteria

1. WHEN JWT token divalidasi, THE SecurityFilter SHALL membaca claim "roles" dan memuat semua permission terkait dari database
2. THE SecurityFilter SHALL menyimpan daftar permission sebagai GrantedAuthority di SecurityContext
3. THE Auth_Service SHALL melindungi endpoint `/api/roles/**` hanya untuk user dengan permission "role_management"
4. THE Auth_Service SHALL melindungi endpoint `/api/users/**` (kecuali `/api/users/me` dan `/api/users/me/password` dan `/api/users/me/permissions`) hanya untuk user dengan permission "user_management"
5. IF user tidak memiliki permission yang diperlukan, THEN THE Auth_Service SHALL mengembalikan HTTP 403 Forbidden

### Requirement 11: Migration Strategy dari Kolom Role Lama

**User Story:** Sebagai sistem, saya ingin memigrasikan data role lama ke struktur baru, sehingga data user yang sudah ada tetap konsisten.

#### Acceptance Criteria

1. THE Migration_Script SHALL membuat entry di tabel `auth.roles` untuk setiap nilai distinct dari kolom `role` di tabel `auth.users`
2. THE Migration_Script SHALL membuat entry di tabel `auth.user_roles` untuk setiap user yang memiliki nilai kolom `role` tidak null
3. THE Migration_Script SHALL mempertahankan kolom `role` di tabel `auth.users` selama masa transisi (tidak dihapus)
4. WHEN semua service sudah menggunakan tabel baru, THE Auth_Service SHALL mendukung penghapusan kolom `role` dari tabel `auth.users` melalui script DDL terpisah

### Requirement 12: CRUD Permission

**User Story:** Sebagai Admin, saya ingin membuat, melihat, mengubah, dan menghapus permission, sehingga saya dapat mengelola daftar halaman/fitur yang tersedia di sistem.

#### Acceptance Criteria

1. WHEN Admin mengirim request GET `/api/permissions`, THE Auth_Service SHALL mengembalikan daftar semua permission beserta deskripsinya
2. WHEN Admin mengirim request POST `/api/permissions` dengan body berisi permission_name dan description, THE Auth_Service SHALL membuat permission baru dan mengembalikan data permission yang dibuat
3. WHEN Admin mengirim request PUT `/api/permissions/{id}` dengan body berisi permission_name dan description, THE Auth_Service SHALL mengubah data permission yang bersangkutan
4. WHEN Admin mengirim request DELETE `/api/permissions/{id}`, THE Auth_Service SHALL menghapus permission beserta relasi role_permissions yang terkait
5. IF permission_name yang dikirim sudah ada di database, THEN THE Auth_Service SHALL mengembalikan error dengan pesan "Permission sudah ada"
6. IF permission yang akan dihapus masih digunakan oleh role, THEN THE Auth_Service SHALL mengembalikan error dengan pesan "Permission masih digunakan oleh role"
