package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChatbotService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IProductService productService;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    public String processChatbotRequest(String userInput) {
        try {
            // Gọi OpenAI API để phân tích yêu cầu
            String openAiResponse = callOpenAiApi(userInput);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJson = mapper.readTree(openAiResponse);
            String criteria = responseJson.path("choices").get(0).path("message").path("content").asText();

            // Trích xuất tiêu chí
            String brand = extractBrand(criteria);
            Integer maxPrice = extractMaxPrice(criteria);

            // Tìm sản phẩm từ ProductService
            List<Product> products = findProducts(brand, maxPrice);

            // Định dạng phản hồi
            StringBuilder response = new StringBuilder("Dựa trên yêu cầu của bạn, tôi đề xuất:\n");
            if (products.isEmpty()) {
                response.append("Không tìm thấy sản phẩm phù hợp.");
            } else {
                for (Product product : products) {
                    response.append(String.format("- %s: Giá %,d VND, Camera %s, Còn %d hàng. Xem chi tiết: /dashboard/products/detail/%d\n",
                            product.getName(), product.getSellingPrice().intValue(), product.getCamera(),
                            product.getStockQuantity(), product.getProductID()));
                    if (!product.getImages().isEmpty()) {
                        response.append(String.format("[Ảnh: %s]\n", product.getImages().get(0).getImageUrl()));
                    }
                }
            }
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Đã có lỗi xảy ra khi xử lý yêu cầu. Vui lòng thử lại!";
        }
    }

    private String callOpenAiApi(String userInput) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.set("Content-Type", "application/json");

        String requestBody = String.format(
                "{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"system\", \"content\": \"You are an assistant that analyzes user requests for phone products and extracts criteria such as brand, price range, or features.\"}, {\"role\": \"user\", \"content\": \"Analyze this request and extract product criteria: %s\"}]}",
                userInput.replace("\"", "\\\"")
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(openAiApiUrl, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    private String extractBrand(String criteria) {
        criteria = criteria.toLowerCase();
        if (criteria.contains("samsung")) return "Samsung";
        if (criteria.contains("xiaomi")) return "Xiaomi";
        if (criteria.contains("apple") || criteria.contains("iphone")) return "Apple";
        return null;
    }

    private Integer extractMaxPrice(String criteria) {
        criteria = criteria.toLowerCase();
        if (criteria.contains("dưới 10 triệu")) return 10_000_000;
        if (criteria.contains("dưới 5 triệu")) return 5_000_000;
        if (criteria.contains("dưới 15 triệu")) return 15_000_000;
        return null;
    }

    private List<Product> findProducts(String brand, Integer maxPrice) {
        // Sử dụng ProductService để tìm sản phẩm
        if (brand != null && maxPrice != null) {
            return productService.searchProductByNameAndSupplier_NameAndPurchasePrice(
                    brand, null, maxPrice, null).getContent();
        } else if (brand != null) {
            return productService.searchProductByNameAndSupplier_NameAndPurchasePrice(
                    brand, null, 0, null).getContent();
        } else if (maxPrice != null) {
            return productService.searchProductByNameAndSupplier_NameAndPurchasePrice(
                    null, null, maxPrice, null).getContent();
        }
        return productService.findAllProducts();
    }
}