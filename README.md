⚙️ Mục tiêu lớp 1: Xử lý file trực tiếp – không liên quan đến API bên ngoài
📌 Tập trung vào các chức năng:
Trích xuất và phân tích chữ ký từ file
Tính toán hash file và xác minh chữ ký
Kiểm tra tình trạng chứng thư
Trả về thông tin người ký và trạng thái

🧩 1. Danh sách các loại chữ ký số cần xử lý (Phân theo dạng lưu trữ)
1. Chữ ký nhúng: Chữ ký nằm trực tiếp trong file	PDF (PAdES), XML (XAdES), DOCX
2. Chữ ký rời: Chữ ký tách rời với file gốc	File .sig, .p7s, .xml kèm hình ảnh/video

📦 2. Các trường hợp (case) cần xử lý
✅ A. Trường hợp chữ ký nhúng
PDF ký số (PAdES):
Một file có thể chứa nhiều chữ ký
Cần trích chữ ký từ metadata (CMS hoặc DSS)
DOCX/OOXML ký số:
Chữ ký nằm trong thư mục \word\_xmlsignatures bên trong file zip
XML ký số (XAdES):
Chữ ký nằm trong các tag <ds:Signature> hoặc <xades:Signature>

✅ B. Trường hợp chữ ký rời
Dữ liệu gốc (ảnh/video/văn bản) + file chữ ký (.sig hoặc .p7s)
Chữ ký thường ở định dạng CMS/PKCS#7
Cần khớp digest giữa file và nội dung trong chữ ký rời

🔄 3. Quy trình xử lý tổng quát (FLOW)
[Bước 1] Nhận file đầu vào (PDF/XML/DOCX + optional chữ ký rời)
[Bước 2] Phân tích loại file
→ Kiểm tra xem là nhúng hay rời
[Bước 3] Trích xuất chữ ký
→ Với file nhúng: phân tích bên trong để lấy CMS
→ Với file rời: lấy file chữ ký (p7s) riêng
[Bước 4] Tính lại hash (digest) của dữ liệu gốc theo thuật toán trong chữ ký
[Bước 5] Xác minh chữ ký:
→ Giải mã CMS
→ Kiểm tra khớp digest
→ Kiểm tra chứng thư (serial, issuer, thời hạn...)
[Bước 6] Trả kết quả:
→ Tên người ký, Serial, Thời gian ký
→ Trạng thái: Hợp lệ / Hết hạn / Bị thu hồi / Không hợp lệ
📤 4. Dữ liệu trả về
{
  "signer_name": "Nguyễn Văn A",
  "certificate_serial": "12AB34CD56",
  "signing_time": "2025-05-20T10:15:00Z",
  "signature_status": "Hợp lệ",  // Các trạng thái: Hợp lệ, Hết hạn, Bị thu hồi, Không hợp lệ
  "details": "Chữ ký trùng khớp hash, chứng thư còn hiệu lực đến 2026-01-01"
}

Bổ sung các case có thể xảy ra
Ngoài các case bạn đã liệt kê, còn có thể có:
C. Chữ ký nhúng khác:
Excel (.xlsx) với chữ ký số
PowerPoint (.pptx) với chữ ký số
ODT/ODS (OpenDocument) với chữ ký số
RTF với chữ ký nhúng

D. Chữ ký đặc biệt:
Multiple signatures trên cùng 1 file (counter-signature)
Timestamp signature (TSA - Time Stamping Authority)
Long-term validation (LTV) signatures
Chữ ký bị corrupt hoặc file bị modify sau khi ký

E. Định dạng chữ ký rời mở rộng:
.asc (ASCII armored PGP)
.der/.cer (Certificate files)
Detached XML signatures

// Project Structure:
/*
src/
├── main/
│   ├── java/
│   │   └── com/digitalsign/
│   │       ├── DigitalSignatureApplication.java
│   │       ├── controller/
│   │       │   └── SignatureVerificationController.java
│   │       ├── service/
│   │       │   ├── SignatureVerificationService.java
│   │       │   ├── FileTypeDetectionService.java
│   │       │   └── impl/
│   │       │       ├── SignatureVerificationServiceImpl.java
│   │       │       └── FileTypeDetectionServiceImpl.java
│   │       ├── processor/
│   │       │   ├── SignatureProcessor.java
│   │       │   ├── pdf/
│   │       │   │   └── PdfSignatureProcessor.java
│   │       │   ├── office/
│   │       │   │   ├── DocxSignatureProcessor.java
│   │       │   │   ├── XlsxSignatureProcessor.java
│   │       │   │   └── PptxSignatureProcessor.java
│   │       │   ├── xml/
│   │       │   │   └── XmlSignatureProcessor.java
│   │       │   └── detached/
│   │       │       └── DetachedSignatureProcessor.java
│   │       ├── model/
│   │       │   ├── SignatureVerificationResult.java
│   │       │   ├── SignatureInfo.java
│   │       │   ├── CertificateInfo.java
│   │       │   └── VerificationStatus.java
│   │       ├── util/
│   │       │   ├── CryptoUtils.java
│   │       │   ├── FileUtils.java
│   │       │   └── CertificateUtils.java
│   │       ├── exception/
│   │       │   ├── SignatureVerificationException.java
│   │       │   ├── UnsupportedFileTypeException.java
│   │       │   └── InvalidSignatureException.java
│   │       └── config/
│   │           └── SecurityConfig.java
│   └── resources/
│       ├── application.yml
│       └── static/
└── test/
    └── java/
        └── com/digitalsign/
            ├── service/
            └── processor/
*/

TOKEN GITHUB: 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
