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
        long currentCount = productRepositoryPort.findAll().size();
        if (currentCount >= 20) {
            log.info("[ProductDataSeeder] MongoDB đã có sẵn {} sản phẩm phong phú, bỏ qua nạp mẫu.", currentCount);
            return;
        }

        log.info("[ProductDataSeeder] Bắt đầu tự động khởi tạo 24 sản phẩm phong phú thực tế vào MongoDB...");

        List<Product> seedProducts = List.of(
                // ==========================================
                // FLASH SALE PRODUCTS (Hàng Sale Giới Hạn)
                // ==========================================
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
                                "RAM / SSD", "16GB DDR5 / 512GB NVMe PCIe 4.0",
                                "Màn hình", "16\" QHD+ 240Hz 100% DCI-P3"
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
                                "Kết nối", "Bluetooth 5.2, LDAC, Multi-point",
                                "Trọng lượng", "250g"
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
                                "Tính năng", "Đo nhịp tim, SpO2, ECG, Phát hiện té ngã",
                                "Chống nước", "WR50 (50 mét)"
                        ))
                        .isFlashSale(true)
                        .build(),

                // ==========================================
                // CATALOG PRODUCTS (Danh Mục Sản Phẩm Đa Dạng)
                // ==========================================
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
                        .name("Tai nghe Bluetooth Apple AirPods Pro Gen 2 USB-C")
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
                        .name("Nồi chiên không dầu Philips HD9252/90 4.1 Lít Rapid Air")
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
                                "Thời lượng pin", "Up to 22 ngày (smartwatch)",
                                "Độ bền", "Chuẩn quân đội MIL-STD-810G"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-7")
                        .name("Máy tính bảng Apple iPad Air 5 M1 10.9 inch Wi-Fi 64GB")
                        .category("Laptop")
                        .price(BigDecimal.valueOf(16990000))
                        .discountPrice(BigDecimal.valueOf(2500000))
                        .discountPercent(14)
                        .imageUrl("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(190)
                        .stockCount(35)
                        .description("Sức mạnh vượt bậc từ vi xử lý Apple M1, hỗ trợ Apple Pencil 2 và Magic Keyboard biến iPad thành cỗ máy làm việc.")
                        .specs(Map.of(
                                "Chipset", "Apple M1 8-core CPU",
                                "RAM / ROM", "8GB / 64GB",
                                "Màn hình", "10.9\" Liquid Retina True Tone"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-8")
                        .name("Robot hút bụi lau nhà Dreame L10s Ultra Tự động giặt sấy giẻ")
                        .category("Đồ gia dụng")
                        .price(BigDecimal.valueOf(18990000))
                        .discountPrice(BigDecimal.valueOf(3000000))
                        .discountPercent(15)
                        .imageUrl("https://images.unsplash.com/photo-1518770660439-4636190af475?w=500&auto=format&fit=crop&q=60")
                        .rating(4.8)
                        .soldCount(88)
                        .stockCount(16)
                        .description("Trạm sạc đa năng All-in-One tự động hút rác, giặt giẻ, sấy khô bằng khí nóng và tự pha nước lau sàn thông minh.")
                        .specs(Map.of(
                                "Lực hút", "5300Pa siêu mạnh",
                                "Dung lượng pin", "5200 mAh",
                                "Hệ thống điều hướng", "AI Action + 3D Structured Light"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-9")
                        .name("Chuột Không dây Công thái học Logitech MX Master 3S")
                        .category("Phụ kiện")
                        .price(BigDecimal.valueOf(2690000))
                        .discountPrice(BigDecimal.valueOf(400000))
                        .discountPercent(15)
                        .imageUrl("https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(410)
                        .stockCount(50)
                        .description("Cảm biến quang học 8000 DPI theo dõi trên mọi bề mặt kể cả kính, nút bấm Silent Clicks êm ái giảm 90% tiếng ồn.")
                        .specs(Map.of(
                                "Cảm biến", "Darkfield 8000 DPI",
                                "Cuộn lăn", "MagSpeed điện từ siêu nhanh",
                                "Pin", "Sạc Type-C dùng tới 70 ngày"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-10")
                        .name("Loa Bluetooth Marshall Stanmore III 80W Âm thanh Hi-Fi")
                        .category("Phụ kiện")
                        .price(BigDecimal.valueOf(10990000))
                        .discountPrice(BigDecimal.valueOf(1500000))
                        .discountPercent(13)
                        .imageUrl("https://images.unsplash.com/photo-1545454675-3531b543be5d?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(96)
                        .stockCount(18)
                        .description("Thiết kế Vintage sang trọng đậm chất Rock 'n' Roll, dải âm trường rộng hơn với công nghệ Dynamic Loudness.")
                        .specs(Map.of(
                                "Công suất", "80W Class D Amplifier",
                                "Kết nối", "Bluetooth 5.2, AUX 3.5mm, RCA",
                                "Dải tần", "45Hz - 20,000Hz"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-11")
                        .name("Màn hình đồ họa chuyên nghiệp LG UltraFine 27 inch 4K IPS")
                        .category("Phụ kiện")
                        .price(BigDecimal.valueOf(12990000))
                        .discountPrice(BigDecimal.valueOf(2000000))
                        .discountPercent(15)
                        .imageUrl("https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&auto=format&fit=crop&q=60")
                        .rating(4.8)
                        .soldCount(74)
                        .stockCount(20)
                        .description("Độ phân giải 4K UHD sắc nét, chuẩn màu 99% DCI-P3 và hỗ trợ kết nối USB-C sạc ngược 90W tiện lợi cho MacBook.")
                        .specs(Map.of(
                                "Kích thước / Tấm nền", "27 inch IPS 4K (3840x2160)",
                                "Độ bao phủ màu", "99% DCI-P3, HDR400",
                                "Cổng kết nối", "USB-C 90W, HDMI 2.0, DisplayPort 1.4"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-12")
                        .name("Máy lọc không khí Xiaomi Smart Air Purifier 4 Pro")
                        .category("Đồ gia dụng")
                        .price(BigDecimal.valueOf(5490000))
                        .discountPrice(BigDecimal.valueOf(1200000))
                        .discountPercent(21)
                        .imageUrl("https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=500&auto=format&fit=crop&q=60")
                        .rating(4.8)
                        .soldCount(310)
                        .stockCount(40)
                        .description("Lọc sạch 99.97% bụi mịn PM2.5, phấn hoa và vi khuẩn trong phòng diện tích lên đến 60m², điều khiển qua Mi Home App.")
                        .specs(Map.of(
                                "Công suất lọc CADR", "500 m³/h (Khử khuẩn Ion âm)",
                                "Diện tích sử dụng", "35 - 60 m²",
                                "Độ ồn", "33.7 dB (Chế độ ban đêm)"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-13")
                        .name("Điện thoại Xiaomi 14 Ultra 16GB/512GB Ống kính Leica Summilux")
                        .category("Điện thoại")
                        .price(BigDecimal.valueOf(31990000))
                        .discountPrice(BigDecimal.valueOf(3000000))
                        .discountPercent(9)
                        .imageUrl("https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(115)
                        .stockCount(25)
                        .description("Hệ thống 4 camera Leica 50MP với cảm biến 1 inch thế hệ mới, màn hình 2K WQHD+ 120Hz siêu sáng 3000 nits.")
                        .specs(Map.of(
                                "Chipset", "Snapdragon 8 Gen 3 (4nm)",
                                "Camera chính", "50MP 1-inch LYT-900 Variable Aperture",
                                "Pin / Sạc", "5000 mAh, Sạc nhanh có dây 90W / Không dây 80W"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-14")
                        .name("Laptop Dell XPS 13 Plus 9320 Core i7-1360P / 16GB / 512GB")
                        .category("Laptop")
                        .price(BigDecimal.valueOf(45990000))
                        .discountPrice(BigDecimal.valueOf(6000000))
                        .discountPercent(13)
                        .imageUrl("https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=500&auto=format&fit=crop&q=60")
                        .rating(4.8)
                        .soldCount(52)
                        .stockCount(14)
                        .description("Tuyệt tác thiết kế tương lai với bàn di chuột vô hình kính phản hồi xúc giác, bàn phím Zero-lattice tràn viền thanh lịch.")
                        .specs(Map.of(
                                "CPU", "Intel Core i7-1360P (12 cores, 16 threads)",
                                "Màn hình", "13.4\" 3.5K OLED Touchscreen",
                                "Trọng lượng", "1.23 kg siêu nhẹ"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-15")
                        .name("Đồng hồ thể thao chuyên nghiệp Garmin Forerunner 265 Music")
                        .category("Đồng hồ")
                        .price(BigDecimal.valueOf(11690000))
                        .discountPrice(BigDecimal.valueOf(1700000))
                        .discountPercent(14)
                        .imageUrl("https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(130)
                        .stockCount(28)
                        .description("Màn hình AMOLED sắc nét rực rỡ, tích hợp kế hoạch luyện tập chuyên sâu Garmin Coach và GPS đa băng tần chính xác tuyệt đối.")
                        .specs(Map.of(
                                "Màn hình", "1.3\" AMOLED Always-On",
                                "Thời lượng pin", "Up to 13 ngày (smartwatch), 20 giờ GPS",
                                "Bộ nhớ nhạc", "Lưu trữ tới 500 bài hát offline"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-16")
                        .name("Máy hút bụi không dây Dyson V12 Detect Slim Total Clean")
                        .category("Đồ gia dụng")
                        .price(BigDecimal.valueOf(19990000))
                        .discountPrice(BigDecimal.valueOf(3000000))
                        .discountPercent(15)
                        .imageUrl("https://images.unsplash.com/photo-1558317374-067fb5f30001?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(98)
                        .stockCount(15)
                        .description("Công nghệ tia Laser xanh phát hiện hạt bụi siêu nhỏ vô hình, cảm biến Piezo tự động tăng lực hút khi gặp nhiều bụi.")
                        .specs(Map.of(
                                "Lực hút", "150 AW",
                                "Thời lượng pin", "Tối đa 60 phút hoạt động liên tục",
                                "Trọng lượng", "2.2 kg"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-17")
                        .name("Máy ảnh Mirrorless Sony Alpha A7 IV Body (Chính Hãng)")
                        .category("Phụ kiện")
                        .price(BigDecimal.valueOf(55990000))
                        .discountPrice(BigDecimal.valueOf(6000000))
                        .discountPercent(10)
                        .imageUrl("https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(40)
                        .stockCount(10)
                        .description("Cảm biến Full-frame Exmor R BSI 33MP, bộ xử lý hình ảnh BIONZ XR đỉnh cao, quay phim 4K 60p 10-bit 4:2:2 chuẩn điện ảnh.")
                        .specs(Map.of(
                                "Cảm biến", "33MP Full-Frame Exmor R CMOS",
                                "Quay video", "4K 60p, 10-bit 4:2:2, S-Cinetone",
                                "Lấy nét", "759 điểm lấy nét theo pha Real-time Eye AF"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-18")
                        .name("Máy cạo râu thông minh Philips S5588 Series 5000 SkinIQ")
                        .category("Đồ gia dụng")
                        .price(BigDecimal.valueOf(2390000))
                        .discountPrice(BigDecimal.valueOf(500000))
                        .discountPercent(20)
                        .imageUrl("https://images.unsplash.com/photo-1621607512214-68297480165e?w=500&auto=format&fit=crop&q=60")
                        .rating(4.7)
                        .soldCount(220)
                        .stockCount(45)
                        .description("Công nghệ cảm biến SkinIQ tự động điều chỉnh tốc độ cắt theo mật độ râu, lưỡi cạo SteelPrecision 45 lưỡi tự mài bén.")
                        .specs(Map.of(
                                "Lưỡi dao", "SteelPrecision 90,000 nhát cắt/phút",
                                "Chống nước", "100% Wet & Dry cạo khô hoặc ướt",
                                "Pin", "Sạc 1 giờ dùng 60 phút"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-19")
                        .name("Đồng hồ Samsung Galaxy Watch 6 Classic 47mm Viền xoay")
                        .category("Đồng hồ")
                        .price(BigDecimal.valueOf(9490000))
                        .discountPrice(BigDecimal.valueOf(1500000))
                        .discountPercent(15)
                        .imageUrl("https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=500&auto=format&fit=crop&q=60")
                        .rating(4.8)
                        .soldCount(145)
                        .stockCount(30)
                        .description("Viền xoay vật lý trứ danh tái xuất, màn hình Sapphire siêu bền, theo dõi thành phần cơ thể BIA và giấc ngủ chuyên sâu.")
                        .specs(Map.of(
                                "Màn hình", "1.5\" Super AMOLED (480x480) Sapphire Crystal",
                                "Khung viền", "Thép không gỉ cao cấp 47mm",
                                "Cảm biến", "Samsung BioActive (BIA, ECG, HR)"
                        ))
                        .isFlashSale(false)
                        .build(),

                Product.builder()
                        .id("cat-20")
                        .name("Bàn chải điện thông minh Oral-B iO Series 9 AI Magnetic")
                        .category("Đồ gia dụng")
                        .price(BigDecimal.valueOf(6490000))
                        .discountPrice(BigDecimal.valueOf(1000000))
                        .discountPercent(15)
                        .imageUrl("https://images.unsplash.com/photo-1559591937-e111a437f14b?w=500&auto=format&fit=crop&q=60")
                        .rating(4.9)
                        .soldCount(80)
                        .stockCount(25)
                        .description("Công nghệ truyền động từ tính iO mang tính cách mạng kết hợp rung vi mô, màn hình tương tác màu sắc và theo dõi 3D 16 vùng răng.")
                        .specs(Map.of(
                                "Chế độ chải", "7 chế độ thông minh (Daily Clean, Whitening, Gum Care...)",
                                "Cảm biến lực", "Smart Pressure Sensor đèn báo 3 màu",
                                "Hộp sạc du lịch", "Power2Go sạc tiện lợi"
                        ))
                        .isFlashSale(false)
                        .build()
        );

        seedProducts.forEach(productRepositoryPort::save);
        log.info("[ProductDataSeeder] Đã nạp thành công {} sản phẩm phong phú thực tế vào MongoDB!", seedProducts.size());
    }
}
