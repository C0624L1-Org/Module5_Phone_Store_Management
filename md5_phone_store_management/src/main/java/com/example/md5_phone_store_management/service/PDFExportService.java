package com.example.md5_phone_store_management.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceDetail;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


@Service
public class PDFExportService {

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Chuyển đổi văn bản tiếng Việt sang không dấu
     */
//    private String removeAccents(String text) {
//        if (text == null) {
//            return "";
//        }
//
//        String temp = Normalizer.normalize(text, Normalizer.Form.NFD);
//        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
//    }

    /**
     * Tạo file PDF từ thông tin hóa đơn
     *
     * @param invoice Hóa đơn cần tạo PDF
     * @param payDate Ngày thanh toán
     * @return ByteArrayInputStream chứa nội dung file PDF
     * @throws DocumentException
     */
    public ByteArrayInputStream generateInvoicePdf(Invoice invoice, String payDate) throws DocumentException {
        // Tạo document mới với kích thước A4
        Document document = new Document(new Rectangle(595, 842)); // A4 size
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = null;

        try {
            // Màu sắc chủ đạo
            BaseColor blueColor = new BaseColor(65, 105, 225); // Màu xanh đậm
            BaseColor lightBlue = new BaseColor(235, 240, 255); // Màu xanh nhạt
            BaseColor redColor = new BaseColor(216, 32, 39); // Màu đỏ
            BaseColor lightRed = new BaseColor(255, 230, 230); // Màu đỏ nhạt
            BaseColor grayBg = new BaseColor(248, 249, 250); // Màu xám nhạt cho background

            // Khai báo biến baseFont với giá trị mặc định null
            BaseFont baseFont = null;
            try {
                // Tải font từ classpath resource stream
                System.out.println("Bắt đầu tải font...");

                // Kiểm tra nếu resourceLoader không null (đã được inject bởi Spring)
                if (resourceLoader != null) {
                    try {
                        Resource resource = resourceLoader.getResource("classpath:static/fonts/RobotoSlab-VariableFont_wght.ttf");
                        if (resource.exists()) {
                            System.out.println("Đã tìm thấy font từ ResourceLoader");
                            baseFont = BaseFont.createFont("RobotoSlab.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, resource.getInputStream().readAllBytes(), null);
                            System.out.println("Đã tải font thành công từ ResourceLoader!");
                        } else {
                            System.out.println("ResourceLoader không tìm thấy font");
                            throw new Exception("ResourceLoader không tìm thấy font");
                        }
                    } catch (Exception e) {
                        System.out.println("Lỗi khi tải font từ ResourceLoader: " + e.getMessage());
                    }
                } else {
                    System.out.println("ResourceLoader không được inject, thử phương pháp khác");
                }

            } catch (Exception e) {
                System.out.println("Lỗi khi tải font: " + e.getMessage());
                e.printStackTrace();
                // Sử dụng font mặc định nếu có lỗi
                try {
                    baseFont = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                    System.out.println("Đã sử dụng font mặc định");
                } catch (Exception ex) {
                    System.out.println("Lỗi khi tải font mặc định: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            // Kiểm tra nếu baseFont vẫn null sau tất cả các nỗ lực, sử dụng font mặc định
            if (baseFont == null) {
                try {
                    baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                    System.out.println("Sử dụng font Helvetica vì không thể tải được font khác");
                } catch (Exception ex) {
                    // Nếu vẫn lỗi, không làm gì thêm, để ứng dụng báo lỗi
                    System.out.println("Không thể tải bất kỳ font nào: " + ex.getMessage());
                }
            }

            // Hàm tạo font với giảm độ đậm
            final int SEMI_BOLD_ALPHA = 200;

            // Tạo font từ BaseFont với hỗ trợ Unicode
            Font titleFont = new Font(baseFont, 18, Font.BOLD, new BaseColor(0, 0, 0, SEMI_BOLD_ALPHA));
            Font subtitleFont = new Font(baseFont, 12, Font.NORMAL, BaseColor.DARK_GRAY);
            Font headerFont = new Font(baseFont, 12, Font.BOLD, new BaseColor(255, 255, 255, SEMI_BOLD_ALPHA));
            Font normalFont = new Font(baseFont, 10, Font.NORMAL, BaseColor.BLACK);
            Font normalBold = new Font(baseFont, 10, Font.BOLD, new BaseColor(0, 0, 0, SEMI_BOLD_ALPHA));
            Font smallFont = new Font(baseFont, 9, Font.NORMAL, BaseColor.DARK_GRAY);
            Font redBoldFont = new Font(baseFont, 12, Font.BOLD, new BaseColor(
                redColor.getRed(), redColor.getGreen(), redColor.getBlue(), SEMI_BOLD_ALPHA));
            Font signatureFont = new Font(baseFont, 12, Font.BOLD, new BaseColor(0, 0, 0, SEMI_BOLD_ALPHA));

            // Font cho watermark
            Font watermarkFont = new Font(baseFont, 30, Font.NORMAL, BaseColor.LIGHT_GRAY);

            // Khởi tạo writer
            writer = PdfWriter.getInstance(document, out);
            document.open();

            // Tạo watermark nhẹ
            PdfContentByte canvas = writer.getDirectContentUnder();
            PdfGState gState = new PdfGState();
            gState.setFillOpacity(0.09f); // Giảm độ đậm của watermark
            canvas.saveState();
            canvas.setGState(gState);

            // Thêm watermark theo đường chéo
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                        new Phrase("Phone Store", watermarkFont),
                        100 + j * 150, 100 + i * 150, 45);
                }
            }

            canvas.restoreState();

            // Tiêu đề hóa đơn
            Paragraph title = new Paragraph("HÓA ĐƠN THANH TOÁN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Thông tin cửa hàng
            Paragraph storeInfo = new Paragraph("PHONE STORE", subtitleFont);
            storeInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(storeInfo);

            // Thêm khoảng trống
            document.add(new Paragraph(" "));

            // Format tiền tệ
            DecimalFormat formatter = new DecimalFormat("#,###");

            // Format ngày tháng
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String formattedDate = payDate;
            try {
                if (payDate.length() > 10) {
                    // Nếu payDate là chuỗi ngày tháng từ VNPay (yyyyMMddHHmmss)
                    if (payDate.matches("\\d{14}")) {
                        Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(payDate);
                        formattedDate = dateFormatter.format(date);
                    }
                }
            } catch (Exception e) {
                // Nếu có lỗi, giữ nguyên giá trị payDate
                formattedDate = payDate;
            }

            // Tạo bảng thông tin hóa đơn
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10);
            infoTable.setSpacingAfter(10);
            infoTable.setWidths(new float[] {1f, 1f});

            // Cột bên trái - thông tin giao dịch
            PdfPCell leftInfoCell = new PdfPCell();
            leftInfoCell.setBorder(Rectangle.BOX);
            leftInfoCell.setPadding(10);
            leftInfoCell.setBorderColor(BaseColor.LIGHT_GRAY);

            Paragraph leftInfoParagraph = new Paragraph();
            leftInfoParagraph.add(new Phrase("Mã giao dịch: ", normalBold));
            leftInfoParagraph.add(new Phrase("#" + invoice.getId() + "\n", normalFont));

            leftInfoParagraph.add(new Phrase("Thời gian thanh toán: ", normalBold));
            leftInfoParagraph.add(new Phrase(formattedDate + "\n", normalFont));

            leftInfoParagraph.add(new Phrase("Số tiền: ", normalBold));
            leftInfoParagraph.add(new Phrase(formatter.format(invoice.getAmount()) + " VND\n", normalFont));

            leftInfoParagraph.add(new Phrase("Nội dung: ", normalBold));
            leftInfoParagraph.add(new Phrase("Thanh toán đơn hàng #" + invoice.getId(), normalFont));

            leftInfoCell.addElement(leftInfoParagraph);
            infoTable.addCell(leftInfoCell);

            // Cột bên phải - thông tin thanh toán
            PdfPCell rightInfoCell = new PdfPCell();
            rightInfoCell.setBorder(Rectangle.BOX);
            rightInfoCell.setPadding(10);
            rightInfoCell.setBorderColor(BaseColor.LIGHT_GRAY);

            Paragraph rightInfoParagraph = new Paragraph();
            rightInfoParagraph.add(new Phrase("Phương thức thanh toán: ", normalBold));
            rightInfoParagraph.add(new Phrase(invoice.getPaymentMethod().getLabel() + "\n", normalFont));

            rightInfoParagraph.add(new Phrase("Trạng thái thanh toán: ", normalBold));
            rightInfoParagraph.add(new Phrase(invoice.getStatus().getLabel() + "\n", normalFont));

            // Thêm thông tin nhân viên phụ trách nếu có
            rightInfoParagraph.add(new Phrase("Nhân viên phụ trách: ", normalBold));
            if (invoice.getEmployee() != null) {
                rightInfoParagraph.add(new Phrase(invoice.getEmployee().getFullName(), normalFont));
            } else {
                rightInfoParagraph.add(new Phrase("Admin", normalFont));
            }

            rightInfoCell.addElement(rightInfoParagraph);
            infoTable.addCell(rightInfoCell);

            document.add(infoTable);

            // Thông tin khách hàng - HEADER
            PdfPTable customerHeaderTable = new PdfPTable(1);
            customerHeaderTable.setWidthPercentage(100);
            customerHeaderTable.setSpacingBefore(10);
            customerHeaderTable.setSpacingAfter(0);

            PdfPCell customerHeaderCell = new PdfPCell();
            customerHeaderCell.setBorder(Rectangle.NO_BORDER);
            customerHeaderCell.setBackgroundColor(blueColor);
            customerHeaderCell.setPadding(8);

            Paragraph customerHeader = new Paragraph("THÔNG TIN KHÁCH HÀNG", headerFont);
            customerHeader.setAlignment(Element.ALIGN_LEFT);
            customerHeaderCell.addElement(customerHeader);
            customerHeaderTable.addCell(customerHeaderCell);
            document.add(customerHeaderTable);

            // Thông tin khách hàng - CONTENT
            PdfPTable customerTable = new PdfPTable(1);
            customerTable.setWidthPercentage(100);
            customerTable.setSpacingBefore(0);
            customerTable.setSpacingAfter(15);

            PdfPCell customerCell = new PdfPCell();
            customerCell.setBorder(Rectangle.BOX);
            customerCell.setBorderColor(BaseColor.LIGHT_GRAY);
            customerCell.setPadding(10);

            Paragraph customerParagraph = new Paragraph();
            customerParagraph.add(new Phrase("Họ tên: ", normalBold));
            customerParagraph.add(new Phrase(invoice.getCustomer().getFullName() + "\n", normalFont));

            customerParagraph.add(new Phrase("Số điện thoại: ", normalBold));
            customerParagraph.add(new Phrase(invoice.getCustomer().getPhone() + "\n", normalFont));

            if (invoice.getCustomer().getEmail() != null && !invoice.getCustomer().getEmail().isEmpty()) {
                customerParagraph.add(new Phrase("Email: ", normalBold));
                customerParagraph.add(new Phrase(invoice.getCustomer().getEmail() + "\n", normalFont));
            }

            if (invoice.getCustomer().getAddress() != null && !invoice.getCustomer().getAddress().isEmpty()) {
                customerParagraph.add(new Phrase("Địa chỉ: ", normalBold));
                customerParagraph.add(new Phrase(invoice.getCustomer().getAddress(), normalFont));
            }

            customerCell.addElement(customerParagraph);
            customerTable.addCell(customerCell);
            document.add(customerTable);

            // Chi tiết sản phẩm - HEADER
            PdfPTable productHeaderTable = new PdfPTable(1);
            productHeaderTable.setWidthPercentage(100);
            productHeaderTable.setSpacingBefore(10);
            productHeaderTable.setSpacingAfter(0);

            PdfPCell productHeaderCell = new PdfPCell();
            productHeaderCell.setBorder(Rectangle.NO_BORDER);
            productHeaderCell.setBackgroundColor(blueColor);
            productHeaderCell.setPadding(8);

            Paragraph productHeader = new Paragraph("CHI TIẾT SẢN PHẨM", headerFont);
            productHeader.setAlignment(Element.ALIGN_LEFT);
            productHeaderCell.addElement(productHeader);
            productHeaderTable.addCell(productHeaderCell);
            document.add(productHeaderTable);

            // Tạo bảng chi tiết sản phẩm
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[] {0.5f, 3f, 2f, 1f, 2f});
            table.setSpacingBefore(0);
            table.setSpacingAfter(15);

            // Thêm header cho bảng
            PdfPCell cell = new PdfPCell();
            cell.setPadding(6);
            cell.setBackgroundColor(new BaseColor(52, 58, 64)); // Màu nền đen của header
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Font headerTableFont = new Font(baseFont, 9, Font.NORMAL, BaseColor.WHITE);

            cell.setPhrase(new Phrase("STT", headerTableFont));
            table.addCell(cell);

            cell.setPhrase(new Phrase("Sản phẩm", headerTableFont));
            table.addCell(cell);

            cell.setPhrase(new Phrase("Đơn giá", headerTableFont));
            table.addCell(cell);

            cell.setPhrase(new Phrase("SL", headerTableFont));
            table.addCell(cell);

            cell.setPhrase(new Phrase("Thành tiền", headerTableFont));
            table.addCell(cell);

            // Thêm dữ liệu vào bảng
            if (invoice.getInvoiceDetailList() != null) {
                int index = 1;
                for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                    // Tạo cell với màu nền xen kẽ
                    PdfPCell dataCell = new PdfPCell();
                    dataCell.setPadding(6);
                    if (index % 2 == 0) {
                        dataCell.setBackgroundColor(grayBg); // Màu nền nhẹ cho hàng chẵn
                    }

                    // STT
                    dataCell.setPhrase(new Phrase(String.valueOf(index++), normalFont));
                    dataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    dataCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(dataCell);

                    // Tên sản phẩm
                    dataCell = new PdfPCell(new Phrase(detail.getProduct().getName(), normalFont));
                    if ((index - 1) % 2 == 0) {
                        dataCell.setBackgroundColor(grayBg);
                    }
                    table.addCell(dataCell);

                    // Đơn giá
                    dataCell = new PdfPCell(new Phrase(formatter.format(detail.getProduct().getSellingPrice()) + " VND", normalFont));
                    dataCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    if ((index - 1) % 2 == 0) {
                        dataCell.setBackgroundColor(grayBg);
                    }
                    table.addCell(dataCell);

                    // Số lượng
                    dataCell = new PdfPCell(new Phrase(String.valueOf(detail.getQuantity()), normalFont));
                    dataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    if ((index - 1) % 2 == 0) {
                        dataCell.setBackgroundColor(grayBg);
                    }
                    table.addCell(dataCell);

                    // Thành tiền
                    dataCell = new PdfPCell(new Phrase(formatter.format(detail.getTotalPrice()) + " VND", normalFont));
                    dataCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    if ((index - 1) % 2 == 0) {
                        dataCell.setBackgroundColor(grayBg);
                    }
                    table.addCell(dataCell);
                }
            }

            document.add(table);

            // Tổng cộng
            PdfPTable totalTable = new PdfPTable(1);
            totalTable.setWidthPercentage(40);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.setSpacingBefore(10);
            totalTable.setSpacingAfter(30);

            PdfPCell totalCell = new PdfPCell();
            totalCell.setBorder(Rectangle.BOX);
            totalCell.setBorderColor(redColor);
            totalCell.setBackgroundColor(lightRed);
            totalCell.setPadding(8);

            Paragraph totalParagraph = new Paragraph("Tổng cộng: " + formatter.format(invoice.getAmount()) + " VND", redBoldFont);
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);

            totalCell.addElement(totalParagraph);
            totalTable.addCell(totalCell);

            document.add(totalTable);

            // Phần chữ ký
            PdfPTable signatureTable = new PdfPTable(3);
            signatureTable.setWidthPercentage(100);
            signatureTable.setSpacingBefore(10);
            signatureTable.setSpacingAfter(20);

            // Cell chữ ký người lập hóa đơn
            PdfPCell signCell1 = new PdfPCell();
            signCell1.setBorder(Rectangle.TOP);
            signCell1.setBorderColor(BaseColor.LIGHT_GRAY);
            signCell1.setPadding(5);
            signCell1.setFixedHeight(60);

            Paragraph signParagraph1 = new Paragraph();
            signParagraph1.setAlignment(Element.ALIGN_CENTER);
            signParagraph1.add(new Phrase("Người lập hóa đơn\n\n", smallFont));

            // Thêm tên nhân viên nếu có
            if (invoice.getEmployee() != null) {
                signParagraph1.add(new Phrase(invoice.getEmployee().getFullName(), signatureFont));
            } else {
                signParagraph1.add(new Phrase("Admin", signatureFont));
            }

            signCell1.addElement(signParagraph1);
            signatureTable.addCell(signCell1);

            // Cell chữ ký kế toán
            PdfPCell signCell2 = new PdfPCell();
            signCell2.setBorder(Rectangle.TOP);
            signCell2.setBorderColor(BaseColor.LIGHT_GRAY);
            signCell2.setPadding(5);
            signCell2.setFixedHeight(60);

            Paragraph signParagraph2 = new Paragraph();
            signParagraph2.setAlignment(Element.ALIGN_CENTER);
            signParagraph2.add(new Phrase("Kế toán\n\n", smallFont));

            signCell2.addElement(signParagraph2);
            signatureTable.addCell(signCell2);

            // Cell chữ ký khách hàng
            PdfPCell signCell3 = new PdfPCell();
            signCell3.setBorder(Rectangle.TOP);
            signCell3.setBorderColor(BaseColor.LIGHT_GRAY);
            signCell3.setPadding(5);
            signCell3.setFixedHeight(60);

            Paragraph signParagraph3 = new Paragraph();
            signParagraph3.setAlignment(Element.ALIGN_CENTER);
            signParagraph3.add(new Phrase("Khách hàng\n\n", smallFont));

            signCell3.addElement(signParagraph3);
            signatureTable.addCell(signCell3);

            document.add(signatureTable);

            // Footer
            PdfPTable footerTable = new PdfPTable(1);
            footerTable.setWidthPercentage(100);

            PdfPCell footerCell = new PdfPCell();
            footerCell.setBorder(Rectangle.NO_BORDER);
            footerCell.setPadding(10);

            Paragraph footerParagraph = new Paragraph();
            footerParagraph.setAlignment(Element.ALIGN_CENTER);
            footerParagraph.add(new Phrase("Cảm ơn quý khách đã mua hàng tại cửa hàng chúng tôi!\n", smallFont));
            footerParagraph.add(new Phrase("Mọi thắc mắc xin liên hệ: 0123 456 789", smallFont));

            footerCell.addElement(footerParagraph);
            footerTable.addCell(footerCell);
            document.add(footerTable);

            document.close();
        } catch (Exception e) {
            throw new DocumentException("Lỗi khi tạo file PDF: " + e.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
