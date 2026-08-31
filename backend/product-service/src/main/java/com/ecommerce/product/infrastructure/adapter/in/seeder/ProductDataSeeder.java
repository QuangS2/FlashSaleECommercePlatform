package com.ecommerce.product.infrastructure.adapter.in.seeder;

import com.ecommerce.product.domain.entity.Product;
import com.ecommerce.product.domain.port.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductDataSeeder implements CommandLineRunner {

    private final ProductRepositoryPort productRepositoryPort;

    @Override
    public void run(String... args) {
        if (!productRepositoryPort.findAll().isEmpty()) {
            log.info("[ProductDataSeeder] MongoDB đã có sẵn dữ liệu sản phẩm, bỏ qua nạp mẫu.");
            return;
        }

        log.info("[ProductDataSeeder] Bắt đầu tự động khởi tạo dữ liệu sản phẩm phong phú vào MongoDB...");

        List<Product> seedProducts = List.of(
                Product.builder()
                        .id("cat-1")
                        .name("Laptop Apple MacBook Air M2 13.6 inch 8GB/256GB")
                        .category("Laptop")
                        .price(BigDecimal.valueOf(28990000))
                        .discountPrice(BigDecimal.valueOf(4500000))
                        .discountPercent(15)
                        .imageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(320)
                        .stockCount(45)
                        .description("MacBook Air M2 siêu mỏng nhẹ, màn hình Liquid Retina sắc nét, thời lượng pin lên tới 18 giờ liên tục.")
                        .specs(Map.of(
                                "CPU", "Apple M2 8-core CPU",
                                "RAM", "8GB Unified Memory",
                                "SSD", "256GB NVMe",
                                "Màn hình", "13.6\" Liquid Retina 500 nits"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-2")
                        .name("Điện thoại Samsung Galaxy S24 Ultra 5G 12GB/256GB - AI Phone")
                        .category("Điện thoại")
                        .price(BigDecimal.valueOf(33990000))
                        .discountPrice(BigDecimal.valueOf(4000000))
                        .discountPercent(11)
                        .imageUrl("https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=500&auto=format&fit=crop&q=60")
                        .rating(4.8)
                        .soldCount(210)
                        .stockCount(28)
                        .description("Quyền năng Galaxy AI đỉnh cao, khung viền Titan bền bỉ, camera 200MP biến đêm thành ngày.")
                        .specs(Map.of(
                                "Màn hình", "Dynamic AMOLED 2X 6.8 inch 120Hz",
                                "Chipset", "Snapdragon 8 Gen 3 for Galaxy",
                                "RAM / ROM", "12GB / 256GB",
                                "Camera", "200MP + 50MP + 12MP + 10MP"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-3")
                        .name("Tai nghe Bluetooth True Wireless Apple AirPods Pro Gen 2 USB-C")
                        .category("Phụ kiện")
                        .price(BigDecimal.valueOf(6190000))
                        .discountPrice(BigDecimal.valueOf(800000))
                        .discountPercent(12)
                        .imageUrl("https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(540)
                        .stockCount(60)
                        .description("Chip H2 mạnh mẽ, tính năng Khử tiếng ồn chủ động gấp 2 lần, cổng sạc USB-C hiện đại.")
                        .specs(Map.of(
                                "Chipset", "Apple H2 headphone chip",
                                "Cổng sạc", "MagSafe USB-C",
                                "Chống nước", "IP54"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-4")
                        .name("Nồi chiên không dầu Philips HD9252/90 4.1 Lít - Công nghệ Rapid Air")
                        .category("Đồ gia dụng")
                        .price(BigDecimal.valueOf(2990000))
                        .discountPrice(BigDecimal.valueOf(1100000))
                        .discountPercent(36)
                        .imageUrl("https://images.unsplash.com/photo-1585515320310-259814833e62?w=500&auto=format&fit=crop&q=60")
                        .rating(4.7)
                        .soldCount(185)
                        .stockCount(19)
                        .description("Giảm 90% lượng chất béo thừa, bảng điều khiển cảm ứng với 7 chương trình nấu cài đặt sẵn.")
                        .specs(Map.of(
                                "Dung tích", "4.1 Lít (Rổ chiên 0.8kg)",
                                "Công suất", "1400W",
                                "Công nghệ", "Rapid Air luồng khí xoáy"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-5")
                        .name("Bàn phím Cơ Không dây Keychron K2 Pro QMK/VIA Swappable")
                        .category("Phụ kiện")
                        .price(BigDecimal.valueOf(2490000))
                        .discountPrice(BigDecimal.valueOf(500000))
                        .discountPercent(20)
                        .imageUrl("https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&auto=format&fit=crop&q=60")
                        .rating(4.8)
                        .soldCount(142)
                        .stockCount(22)
                        .description("Bàn phím cơ layout 75% nâng cấp foam tiêu âm, hỗ trợ QMK/VIA tùy chỉnh phím tự do.")
                        .specs(Map.of(
                                "Switch", "Keychron K Pro Red / Brown",
                                "Kết nối", "Bluetooth 5.1 & Type-C Wired",
                                "Pin", "4000 mAh"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-6")
                        .name("Đồng hồ Garmin Fenix 7 Pro Sapphire Solar Edition")
                        .category("Đồng hồ")
                        .price(BigDecimal.valueOf(23990000))
                        .discountPrice(BigDecimal.valueOf(4000000))
                        .discountPercent(16)
                        .imageUrl("https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(65)
                        .stockCount(12)
                        .description("Đồng hồ thể thao chuyên nghiệp với kính sạc năng lượng mặt trời Power Sapphire, đèn LED chiếu sáng.")
                        .specs(Map.of(
                                "Màn hình", "1.3\" MIP chống chói",
                                "Thời lượng pin", "Up to 22 ngày (chế độ smartwatch)",
                                "Độ bền", "Chuẩn quân đội MIL-STD-810G"
                        ))
                        .isFlashSale(false)
                        .build(),

                // Flash Sale Products
                Product.builder()
                        .id("fs-101")
                        .name("Điện thoại iPhone 15 Pro Max 256GB - Chính hãng VN/A")
                        .category("Điện thoại")
                        .price(BigDecimal.valueOf(34990000))
                        .discountPrice(BigDecimal.valueOf(6000000))
                        .discountPercent(17)
                        .imageUrl("https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(85)
                        .stockCount(15)
                        .description("iPhone 15 Pro Max với khung Titan cao cấp, chip A17 Pro siêu mạnh mẽ và camera Zoom quang học 5x sắc nét.")
                        .specs(Map.of(
                                "Màn hình", "Super Retina XDR OLED 6.7 inch 120Hz",
                                "Chipset", "Apple A17 Pro (3nm)",
                                "RAM / Bộ nhớ", "8GB / 256GB",
                                "Pin", "4,422 mAh, Sạc nhanh 20W"
                        ))
                        .isFlashSale(true)
                        .build(),

                Product.builder()
                        .id("fs-102")
                        .name("Laptop Gaming ASUS ROG Strix G16 i7-13650HX / RTX 4060")
                        .category("Laptop")
                        .price(BigDecimal.valueOf(42990000))
                        .discountPrice(BigDecimal.valueOf(9500000))
                        .discountPercent(22)
                        .imageUrl("https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=500&auto=format&fit=crop&q=60")
                        .rating(4.8)
                        .soldCount(42)
                        .stockCount(8)
                        .description("Sức mạnh vượt trội với Intel Core i7 thế hệ 13 và card đồ họa RTX 4060, màn hình 240Hz siêu mượt.")
                        .specs(Map.of(
                                "CPU", "Intel Core i7-13650HX",
                                "VGA", "NVIDIA GeForce RTX 4060 8GB",
                                "RAM / SSD", "16GB DDR5 / 512GB NVMe PCIe 4.0"
                        ))
                        .isFlashSale(true)
                        .build(),

                Product.builder()
                        .id("fs-103")
                        .name("Tai nghe Chống ồn Sony WH-1000XM5 Hi-Res Audio")
                        .category("Phụ kiện")
                        .price(BigDecimal.valueOf(8490000))
                        .discountPrice(BigDecimal.valueOf(2200000))
                        .discountPercent(26)
                        .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(95)
                        .stockCount(5)
                        .description("Công nghệ chống ồn chủ động ANC đỉnh cao thế giới với 8 micro và bộ xử lý Integrated Processor V1.")
                        .specs(Map.of(
                                "Thời lượng pin", "Up to 30 giờ (bật ANC)",
                                "Kết nối", "Bluetooth 5.2, LDAC, Multi-point"
                        ))
                        .isFlashSale(true)
                        .build(),

                Product.builder()
                        .id("fs-104")
                        .name("Đồng hồ Thông minh Apple Watch Series 9 GPS 45mm")
                        .category("Đồng hồ")
                        .price(BigDecimal.valueOf(11990000))
                        .discountPrice(BigDecimal.valueOf(2500000))
                        .discountPercent(21)
                        .imageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=60")
                        .rating(4.7)
                        .soldCount(18)
                        .stockCount(32)
                        .description("Chip S9 SiP mạnh mẽ hơn, thao tác Double Tap chạm hai lần tiện lợi, màn hình sáng gấp đôi 2000 nits.")
                        .specs(Map.of(
                                "Kích thước", "45mm Viền nhôm",
                                "Tính năng", "Đo nhịp tim, SpO2, ECG, Phát hiện té ngã"
                        ))
                        .isFlashSale(true)
                        .build()
        );

        seedProducts.forEach(productRepositoryPort::save);
        log.info("[ProductDataSeeder] Đã nạp thành công {} sản phẩm mẫu vào MongoDB!", seedProducts.size());
    }
}
