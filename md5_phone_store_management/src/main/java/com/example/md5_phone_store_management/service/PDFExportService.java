//package com.example.md5_phone_store_management.service;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.text.DecimalFormat;
//import java.text.Normalizer;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.regex.Pattern;
//
//import org.springframework.stereotype.Service;
//
//import com.example.md5_phone_store_management.model.Invoice;
//import com.example.md5_phone_store_management.model.InvoiceDetail;
//
//import com.itextpdf.text.BaseColor;
//import com.itextpdf.text.Document;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.Element;
//import com.itextpdf.text.Font;
//import com.itextpdf.text.FontFactory;
//import com.itextpdf.text.Paragraph;
//import com.itextpdf.text.Phrase;
//import com.itextpdf.text.Rectangle;
//import com.itextpdf.text.pdf.PdfPCell;
//import com.itextpdf.text.pdf.PdfPTable;
//import com.itextpdf.text.pdf.PdfWriter;
//
//
//
//
//@Service
//public class PDFExportService {
//
//    /**
//     * Chuyển đổi văn bản tiếng Việt sang không dấu
//     */
//    private String removeAccents(String text) {
//        if (text == null) {
//            return "";
//        }
//
//        String temp = Normalizer.normalize(text, Normalizer.Form.NFD);
//        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
//    }
//
//    /**
//     * Tạo file PDF từ thông tin hóa đơn
//     *
//     * @param invoice Hóa đơn cần tạo PDF
//     * @return ByteArrayInputStream chứa nội dung file PDF
//     * @throws DocumentException
//     */
//    public ByteArrayInputStream generateInvoicePdf(Invoice invoice, String payDate, String bankCode, String cardType) throws DocumentException {
//        // Tạo document mới
//        Document document = new Document();
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        try {
//            // Thiết lập font
//            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
//            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
//            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
//            Font smallBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK);
//            Font redFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(216, 32, 39));
//
//            // Khởi tạo writer
//            PdfWriter.getInstance(document, out);
//            document.open();
//
//            // Tiêu đề hóa đơn
//            Paragraph title = new Paragraph(removeAccents("HOA DON THANH TOAN"), titleFont);
//            title.setAlignment(Element.ALIGN_CENTER);
//            document.add(title);
//
//            // Thông tin cửa hàng
//            Paragraph storeInfo = new Paragraph(removeAccents("CUA HANG DIEN THOAI"), headerFont);
//            storeInfo.setAlignment(Element.ALIGN_CENTER);
//            document.add(storeInfo);
//
//            // Thêm khoảng trống
//            document.add(new Paragraph(" "));
//
//            // Format tiền tệ
//            DecimalFormat formatter = new DecimalFormat("#,###");
//
//            // Format ngày tháng
//            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//            String formattedDate = payDate;
//            try {
//                if (payDate.length() > 10) {
//                    // Nếu payDate là chuỗi ngày tháng từ VNPay (yyyyMMddHHmmss)
//                    if (payDate.matches("\\d{14}")) {
//                        Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(payDate);
//                        formattedDate = dateFormatter.format(date);
//                    }
//                }
//            } catch (Exception e) {
//                // Nếu có lỗi, giữ nguyên giá trị payDate
//                formattedDate = payDate;
//            }
//
//            // Tạo bảng thông tin hóa đơn
//            PdfPTable infoTable = new PdfPTable(1);
//            infoTable.setWidthPercentage(100);
//            infoTable.setSpacingBefore(10);
//            infoTable.setSpacingAfter(10);
//
//            PdfPCell infoCell = new PdfPCell();
//            infoCell.setBorder(Rectangle.BOX);
//            infoCell.setPadding(10);
//            infoCell.setBackgroundColor(new BaseColor(248, 249, 250)); // Màu nền nhẹ
//
//            Paragraph infoParagraph = new Paragraph();
//            infoParagraph.add(new Phrase(removeAccents("Ma giao dich: ") + invoice.getId() + "\n", normalFont));
//            infoParagraph.add(new Phrase(removeAccents("So tien: ") + formatter.format(invoice.getAmount()) + " VND\n", normalFont));
//            infoParagraph.add(new Phrase(removeAccents("Thong tin don hang: ") + removeAccents(invoice.getOrderInfo()) + "\n", normalFont));
//            infoParagraph.add(new Phrase(removeAccents("Ngay thanh toan: ") + formattedDate + "\n", normalFont));
//
//            // Phương thức thanh toán
//            String paymentMethod = bankCode.equals("CASH") ? removeAccents("Tien mat") : "VNPay - " + bankCode;
//            infoParagraph.add(new Phrase(removeAccents("Phuong thuc thanh toan: ") + paymentMethod + "\n", normalFont));
//
//            if (cardType != null && !cardType.equals("CASH")) {
//                infoParagraph.add(new Phrase(removeAccents("Loai the: ") + cardType + "\n", normalFont));
//            }
//
//            infoCell.addElement(infoParagraph);
//            infoTable.addCell(infoCell);
//            document.add(infoTable);
//
//            // Thông tin khách hàng
//            Paragraph customerTitle = new Paragraph(removeAccents("THONG TIN KHACH HANG"), headerFont);
//            document.add(customerTitle);
//            document.add(new Paragraph(" "));
//
//            PdfPTable customerTable = new PdfPTable(1);
//            customerTable.setWidthPercentage(100);
//            customerTable.setSpacingBefore(5);
//            customerTable.setSpacingAfter(15);
//
//            PdfPCell customerCell = new PdfPCell();
//            customerCell.setBorder(Rectangle.BOX);
//            customerCell.setPadding(10);
//
//            Paragraph customerParagraph = new Paragraph();
//            customerParagraph.add(new Phrase(removeAccents("Ho ten: ") + removeAccents(invoice.getCustomer().getFullName()) + "\n", normalFont));
//            customerParagraph.add(new Phrase(removeAccents("So dien thoai: ") + invoice.getCustomer().getPhone() + "\n", normalFont));
//            if (invoice.getCustomer().getEmail() != null && !invoice.getCustomer().getEmail().isEmpty()) {
//                customerParagraph.add(new Phrase("Email: " + invoice.getCustomer().getEmail() + "\n", normalFont));
//            }
//            if (invoice.getCustomer().getAddress() != null && !invoice.getCustomer().getAddress().isEmpty()) {
//                customerParagraph.add(new Phrase(removeAccents("Dia chi: ") + removeAccents(invoice.getCustomer().getAddress()), normalFont));
//            }
//
//            customerCell.addElement(customerParagraph);
//            customerTable.addCell(customerCell);
//            document.add(customerTable);
//
//            // Chi tiết sản phẩm
//            Paragraph productTitle = new Paragraph(removeAccents("CHI TIET SAN PHAM"), headerFont);
//            document.add(productTitle);
//            document.add(new Paragraph(" "));
//
//            // Tạo bảng chi tiết sản phẩm
//            PdfPTable table = new PdfPTable(5);
//            table.setWidthPercentage(100);
//            table.setWidths(new float[] {1f, 3f, 2f, 1f, 2f});
//            table.setSpacingBefore(5);
//            table.setSpacingAfter(15);
//
//            // Thêm header cho bảng
//            PdfPCell cell = new PdfPCell();
//            cell.setPadding(8);
//            cell.setBackgroundColor(new BaseColor(52, 58, 64)); // Màu nền đen của header
//            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//
//            Font headerTableFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE);
//
//            cell.setPhrase(new Phrase("STT", headerTableFont));
//            table.addCell(cell);
//
//            cell.setPhrase(new Phrase(removeAccents("San pham"), headerTableFont));
//            table.addCell(cell);
//
//            cell.setPhrase(new Phrase(removeAccents("Don gia"), headerTableFont));
//            table.addCell(cell);
//
//            cell.setPhrase(new Phrase(removeAccents("So luong"), headerTableFont));
//            table.addCell(cell);
//
//            cell.setPhrase(new Phrase(removeAccents("Thanh tien"), headerTableFont));
//            table.addCell(cell);
//
//            // Thêm dữ liệu vào bảng
//            if (invoice.getInvoiceDetailList() != null) {
//                int index = 1;
//                for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
//                    // Tạo cell với màu nền xen kẽ
//                    PdfPCell dataCell = new PdfPCell();
//                    dataCell.setPadding(8);
//                    if (index % 2 == 0) {
//                        dataCell.setBackgroundColor(new BaseColor(248, 249, 250)); // Màu nền nhẹ cho hàng chẵn
//                    }
//
//                    // STT
//                    dataCell.setPhrase(new Phrase(String.valueOf(index++), normalFont));
//                    dataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    dataCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//                    table.addCell(dataCell);
//
//                    // Tên sản phẩm
//                    dataCell = new PdfPCell(new Phrase(removeAccents(detail.getProduct().getName()), normalFont));
//                    if ((index - 1) % 2 == 0) {
//                        dataCell.setBackgroundColor(new BaseColor(248, 249, 250));
//                    }
//                    table.addCell(dataCell);
//
//                    // Đơn giá
//                    dataCell = new PdfPCell(new Phrase(formatter.format(detail.getProduct().getSellingPrice()) + " VND", normalFont));
//                    dataCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                    if ((index - 1) % 2 == 0) {
//                        dataCell.setBackgroundColor(new BaseColor(248, 249, 250));
//                    }
//                    table.addCell(dataCell);
//
//                    // Số lượng
//                    dataCell = new PdfPCell(new Phrase(String.valueOf(detail.getQuantity()), normalFont));
//                    dataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    if ((index - 1) % 2 == 0) {
//                        dataCell.setBackgroundColor(new BaseColor(248, 249, 250));
//                    }
//                    table.addCell(dataCell);
//
//                    // Thành tiền
//                    dataCell = new PdfPCell(new Phrase(formatter.format(detail.getTotalPrice()) + " VND", normalFont));
//                    dataCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                    if ((index - 1) % 2 == 0) {
//                        dataCell.setBackgroundColor(new BaseColor(248, 249, 250));
//                    }
//                    table.addCell(dataCell);
//                }
//            }
//
//            document.add(table);
//
//            // Tổng cộng
//            Paragraph total = new Paragraph(removeAccents("Tong cong: ") + formatter.format(invoice.getAmount()) + " VND", redFont);
//            total.setAlignment(Element.ALIGN_RIGHT);
//            document.add(total);
//
//            // Thêm khoảng trống
//            document.add(new Paragraph(" "));
//            document.add(new Paragraph(" "));
//
//            // Footer
//            PdfPTable footerTable = new PdfPTable(1);
//            footerTable.setWidthPercentage(100);
//
//            PdfPCell footerCell = new PdfPCell();
//            footerCell.setBorder(Rectangle.TOP);
//            footerCell.setPadding(10);
//
//            Paragraph footerParagraph = new Paragraph();
//            footerParagraph.setAlignment(Element.ALIGN_CENTER);
//            footerParagraph.add(new Phrase(removeAccents("Cam on quy khach da mua hang tai cua hang chung toi!\n"), normalFont));
//            footerParagraph.add(new Phrase(removeAccents("Moi thac mac xin lien he: 0123 456 789"), normalFont));
//
//            footerCell.addElement(footerParagraph);
//            footerTable.addCell(footerCell);
//            document.add(footerTable);
//
//            document.close();
//        } catch (Exception e) {
//            throw new DocumentException(removeAccents("Loi khi tao file PDF: ") + e.getMessage());
//        }
//
//        return new ByteArrayInputStream(out.toByteArray());
//    }
//}
