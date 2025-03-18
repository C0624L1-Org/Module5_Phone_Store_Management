package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.dto.ChangePasswordDTO;
import com.example.md5_phone_store_management.service.IEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.logging.Logger;

@Controller
public class PasswordController {

    private static final Logger logger = Logger.getLogger(PasswordController.class.getName());

    @Autowired
    private IEmployeeService employeeService;

    @GetMapping("/common/change-password")
    public String showChangePasswordPage(Model model) {
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        return "common/change-password";
    }

    @PostMapping("/common/change-password")
    public String changePassword(@ModelAttribute("changePasswordDTO") ChangePasswordDTO changePasswordDTO,
                                 Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Old Password from Form: " + changePasswordDTO.getOldPassword());
        logger.info("New Password: " + changePasswordDTO.getNewPassword());
        logger.info("Confirm Password: " + changePasswordDTO.getConfirmPassword());

        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            logger.warning("Password mismatch between newPassword and confirmPassword!");
            return "common/change-password";
        }

        // Gọi service để đổi mật khẩu
        boolean success = employeeService.changePassword(username,
                changePasswordDTO.getOldPassword(),
                changePasswordDTO.getNewPassword());

        if (success) {
            model.addAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/login"; // Redirect sau khi thành công
        } else {
            model.addAttribute("error", "Mật khẩu cũ không đúng!");
            return "common/change-password";
        }
    }
}