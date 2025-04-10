package com.example.md5_phone_store_management.service.implement;



import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    @Autowired
    private EntityManager entityManager;

    @SuppressWarnings("unchecked") // Loại bỏ cảnh báo unchecked assignment
    public <T> void kiemTraLichSuThayDoi(Class<T> entityClass, Long entityId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        List<Object[]> revisions = auditReader.createQuery()
                .forRevisionsOfEntity(entityClass, true, true)
                .add(AuditEntity.id().eq(entityId))
                .getResultList();

        if (revisions.isEmpty()) {
            System.out.println("Không tìm thấy lịch sử thay đổi cho " + entityClass.getSimpleName() + " với ID: " + entityId);
        } else {
            System.out.println("Lịch sử thay đổi của " + entityClass.getSimpleName() + " với ID: " + entityId);
            for (Object[] revision : revisions) {
                Object entity = revision[0]; // Entity
                Number revisionNumber = (Number) revision[1]; // Số phiên bản
                String revType = revision[2].toString(); // Loại thay đổi
                String hanhDong = switch (revType) {
                    case "0" -> "Thêm mới";
                    case "1" -> "Cập nhật";
                    case "2" -> "Xóa";
                    default -> "Không xác định";
                };
                System.out.println("Phiên bản: " + revisionNumber + " | Hành động: " + hanhDong + " | Dữ liệu: " + entity);
            }
        }
    }
}