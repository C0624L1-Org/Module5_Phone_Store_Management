package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.dto.EmployeeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import com.example.md5_phone_store_management.service.ICustomerService;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.implement.SupplierService;

import jakarta.validation.Valid;

@Controller
public class ProfileController {

    @Autowired
    private IEmployeeRepository employeeRepository;
    
    @Autowired
    private IEmployeeService employeeService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ICustomerService customerService;

    @GetMapping("/common/profile")
    public String profile(Model model) {
        // Lấy thông tin người dùng hiện tại từ Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        // Lấy username của người dùng đã đăng nhập
        String username = authentication.getName();

        // Truy vấn thông tin employee từ database dựa trên username
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng với username: " + username));

        model.addAttribute("employee", employee);
        model.addAttribute("role", employee.getRole() != null ? employee.getRole().name() : "Không xác định");
        model.addAttribute("roleLabel", employee.getRole().getLabel());

        return "common/profile";
    }
    
    @GetMapping("/common/edit-profile")
    public String showEditProfileForm(Model model) {
        // Lấy thông tin người dùng hiện tại từ Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        // Lấy username của người dùng đã đăng nhập
        String username = authentication.getName();

        // Truy vấn thông tin employee từ database dựa trên username
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng với username: " + username));

        // Thêm thông tin employee vào model
        model.addAttribute("employee", employee);

        return "common/edit-profile";
    }
    
    @PostMapping("/common/update-profile")
    public String updateProfile(@Valid @ModelAttribute("employee") EmployeeDTO employeeUpdate,
                              BindingResult bindingResult,
                              @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                              @RequestParam(value = "currentPassword", required = false) String currentPassword,
                              @RequestParam(value = "newPassword", required = false) String newPassword,
                              @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                              @RequestParam(value = "changePassword", required = false) String changePassword,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        // Lấy thông tin người dùng hiện tại từ Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Lấy thông tin nhân viên hiện tại từ database
        Employee currentEmployee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng với username: " + username));
        
        // Nếu có lỗi validation, quay lại form chỉnh sửa
        employeeUpdate.validate(employeeUpdate, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("employee", employeeUpdate);
            return "common/edit-profile";
        }
        
        // Kiểm tra trùng email và số điện thoại
        if (!currentEmployee.getEmail().equals(employeeUpdate.getEmail()) &&
                (employeeService.existsByEmail(employeeUpdate.getEmail())
                        || customerService.isEmailExists(employeeUpdate.getEmail())
                        || supplierService.existsByEmail(employeeUpdate.getEmail()))) {
            bindingResult.rejectValue("email", "duplicate", "Email này đã được sử dụng");
            model.addAttribute("employee", employeeUpdate);
            return "common/edit-profile";
        }

        if (!currentEmployee.getPhone().equals(employeeUpdate.getPhone()) &&
                (employeeService.existsByPhone(employeeUpdate.getPhone())
                        || customerService.isPhoneExists(employeeUpdate.getPhone())
                        || supplierService.existsByPhone(employeeUpdate.getPhone()))) {
            bindingResult.rejectValue("phone", "duplicate", "Số điện thoại này đã được sử dụng");
            model.addAttribute("employee", employeeUpdate);
            return "common/edit-profile";
        }
        
        // Xử lý thay đổi mật khẩu nếu checkbox được chọn
        boolean hasPasswordError = false;
        if (changePassword != null || currentPassword != null && !currentPassword.isEmpty()) {
            // Kiểm tra mật khẩu mới và xác nhận mật khẩu
            if (newPassword == null || newPassword.isEmpty()) {
                model.addAttribute("newPasswordError", "Mật khẩu mới không được để trống");
                hasPasswordError = true;
            } else if (newPassword.length() < 6 || newPassword.length() > 30) {
                model.addAttribute("newPasswordError", "Mật khẩu mới phải từ 6 đến 30 ký tự!");
                hasPasswordError = true;
            }
            
            if (confirmPassword == null || confirmPassword.isEmpty()) {
                model.addAttribute("confirmPasswordError", "Vui lòng xác nhận mật khẩu mới");
                hasPasswordError = true;
            } else if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("confirmPasswordError", "Mật khẩu xác nhận không khớp với mật khẩu mới");
                hasPasswordError = true;
            }
            
            if (currentPassword == null || currentPassword.isEmpty()) {
                model.addAttribute("currentPasswordError", "Vui lòng nhập mật khẩu hiện tại");
                hasPasswordError = true;
            }
            
            // Nếu có lỗi mật khẩu, quay lại form với dữ liệu đã nhập
            if (hasPasswordError) {
                model.addAttribute("employee", employeeUpdate);
                model.addAttribute("currentPassword", currentPassword);
                model.addAttribute("newPassword", newPassword);
                model.addAttribute("confirmPassword", confirmPassword);
                return "common/edit-profile";
            }
            
            // Kiểm tra mật khẩu hiện tại có đúng không
            boolean passwordChanged = employeeService.changePassword(username, currentPassword, newPassword);
            if (!passwordChanged) {
                model.addAttribute("currentPasswordError", "Mật khẩu hiện tại không đúng");
                model.addAttribute("employee", employeeUpdate);
                model.addAttribute("currentPassword", currentPassword);
                model.addAttribute("newPassword", newPassword);
                model.addAttribute("confirmPassword", confirmPassword);
                return "common/edit-profile";
            }
            
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Mật khẩu đã được thay đổi thành công");
        }
        
        // Xử lý upload avatar nếu có
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Kiểm tra kích thước file (tối đa 10MB)
            if (avatarFile.getSize() > 10 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("messageType", "error");
                redirectAttributes.addFlashAttribute("message", "Kích thước ảnh quá lớn (tối đa 10MB)!");
                model.addAttribute("employee", employeeUpdate);
                return "common/edit-profile";
            }
            
            // Kiểm tra định dạng file
            if (!avatarFile.getContentType().startsWith("image/")) {
                redirectAttributes.addFlashAttribute("messageType", "error");
                redirectAttributes.addFlashAttribute("message", "Định dạng ảnh không hợp lệ!");
                model.addAttribute("employee", employeeUpdate);
                return "common/edit-profile";
            }
            
            // Upload avatar thông qua EmployeeService
            try {
                employeeService.updateAvatar(currentEmployee.getEmployeeID(), avatarFile);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("messageType", "error");
                redirectAttributes.addFlashAttribute("message", "Lỗi khi upload avatar: " + e.getMessage());
                model.addAttribute("employee", employeeUpdate);
                return "common/edit-profile";
            }
        }
        
        // Cập nhật thông tin nhân viên
        currentEmployee.setFullName(employeeUpdate.getFullName() != null ? employeeUpdate.getFullName() : currentEmployee.getFullName());
        currentEmployee.setDob(employeeUpdate.getDob() != null ? employeeUpdate.getDob() : currentEmployee.getDob());
        currentEmployee.setAddress(employeeUpdate.getAddress() != null ? employeeUpdate.getAddress() : currentEmployee.getAddress());
        currentEmployee.setPhone(employeeUpdate.getPhone() != null ? employeeUpdate.getPhone() : currentEmployee.getPhone());
        currentEmployee.setEmail(employeeUpdate.getEmail() != null ? employeeUpdate.getEmail() : currentEmployee.getEmail());
        currentEmployee.setPassword(employeeUpdate.getPassword() != null ? employeeUpdate.getPassword() : currentEmployee.getPassword());
        currentEmployee.setAvatar(employeeUpdate.getAvatar() != null ? employeeUpdate.getAvatar() : currentEmployee.getAvatar());
        
        // Lưu thông tin đã cập nhật
        employeeRepository.save(currentEmployee);
        
        // Thêm thông báo thành công
        if (redirectAttributes.getFlashAttributes().get("message") == null) {
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Thông tin cá nhân đã được cập nhật thành công");
        }
        
        return "redirect:/common/profile";
    }
}