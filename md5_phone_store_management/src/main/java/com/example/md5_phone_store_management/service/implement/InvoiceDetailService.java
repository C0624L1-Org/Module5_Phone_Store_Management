package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.InvoiceDetail;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.repository.IInvoiceDetailRepository;
import com.example.md5_phone_store_management.repository.IInvoiceRepository;
import com.example.md5_phone_store_management.repository.IProductRepository;
import com.example.md5_phone_store_management.repository.InvoiceRepository;
import com.example.md5_phone_store_management.service.ICustomerService;
import com.example.md5_phone_store_management.service.IInvoiceDetailService;
import com.example.md5_phone_store_management.service.IInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class InvoiceDetailService implements IInvoiceDetailService {
    @Autowired
    private IInvoiceDetailRepository invoiceDetailRepository;


    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private IProductRepository iProductRepository;

    @Autowired
    private ICustomerService iCustomerService;



    @Override
    public String getTopBuyingCustomerName() {
        Long customerId = invoiceDetailRepository.getTopBuyingCustomerId();
        if (customerId == null || customerId == 0) {
            return "Không có khách hàng"; // Trả về chuỗi mặc định nếu không có khách hàng
        }
        Customer customer = iCustomerService.findCustomerById(Math.toIntExact(customerId));
        if (customer == null) {
            return "Không tìm thấy khách hàng"; // Trường hợp khách hàng không tồn tại
        }
        return customer.getFullName();
    }

    @Override
    public Long getTopBuyingCustomerTotalPurchase() {
        Long customerId = invoiceDetailRepository.getTopBuyingCustomerId();
        return invoiceDetailRepository.getTopBuyingCustomerTotalPurchaseByTopBuyingCustomerId(customerId);
    }

    @Override
    public Integer getTopBuyingCustomerTotalPurchaseQuantity() {
        Long customerId = invoiceDetailRepository.getTopBuyingCustomerId();
        return invoiceRepository.getTopBuyingCustomerTotalPurchaseQuantity(customerId);
    }


    @Override
    public Integer getTopSellingProductNamePurchaseQuantity() {
        Long productId = invoiceDetailRepository.getTopSellingProductId();
        return invoiceDetailRepository.findTopSellingProductNamePurchaseQuantityByProductId(productId);
    }


    @Override
    public String getTopSellingProductName() {
        Long productId = invoiceDetailRepository.getTopSellingProductId();
        if (productId == 0) {
            return "";
        }
        Product topSellingProduct = iProductRepository.findByProductID(Math.toIntExact(productId));
        if (topSellingProduct == null) {
            return ""; // Trường hợp tìm lỗi
        }
        return topSellingProduct.getName();
    }

    @Override
    public Page<InvoiceDetail> findAll(Pageable pageable) {
        return invoiceDetailRepository.findAll(pageable);
    }



//    getTopSellingProductName

    @Override
    public List<InvoiceDetail> findInvoiceDetailById(Long invoiceId) {
        return  invoiceDetailRepository.findInvoiceDetailsByInvoiceId(invoiceId);
    }

    @Override
    public Page<InvoiceDetail> sortByCustomerFullName(Pageable pageable) {
        return invoiceDetailRepository.sortByCustomerFullName(pageable);
    }

    @Override
    public Page<InvoiceDetail> sortByProductName(Pageable pageable) {
        return invoiceDetailRepository.sortByProductName(pageable);
    }

    @Override
    public Page<InvoiceDetail> sortByPrice(Pageable pageable) {
        return invoiceDetailRepository.sortByPrice(pageable);
    }
}
