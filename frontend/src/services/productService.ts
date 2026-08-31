import { Product } from '../types';

const FALLBACK_PRODUCTS: Product[] = [
  {
    id: 'cat-1',
    name: 'Laptop Apple MacBook Air M2 13.6 inch 8GB/256GB',
    category: 'Laptop',
    originalPrice: 28990000,
    salePrice: 24490000,
    discountPercent: 15,
    imageUrl: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 320,
    stockCount: 45,
    description: 'MacBook Air M2 siêu mỏng nhẹ, màn hình Liquid Retina sắc nét, thời lượng pin lên tới 18 giờ liên tục.',
    specs: {
      'CPU': 'Apple M2 8-core CPU',
      'RAM': '8GB Unified Memory',
      'SSD': '256GB NVMe',
      'Màn hình': '13.6" Liquid Retina 500 nits',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-2',
    name: 'Điện thoại Samsung Galaxy S24 Ultra 5G 12GB/256GB - AI Phone',
    category: 'Điện thoại',
    originalPrice: 33990000,
    salePrice: 29990000,
    discountPercent: 11,
    imageUrl: 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=500&auto=format&fit=crop&q=60',
    rating: 4.8,
    soldCount: 210,
    stockCount: 28,
    description: 'Quyền năng Galaxy AI đỉnh cao, khung viền Titan bền bỉ, camera 200MP biến đêm thành ngày.',
    specs: {
      'Màn hình': 'Dynamic AMOLED 2X 6.8 inch 120Hz',
      'Chipset': 'Snapdragon 8 Gen 3 for Galaxy',
      'RAM / ROM': '12GB / 256GB',
      'Camera': '200MP + 50MP + 12MP + 10MP',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-3',
    name: 'Tai nghe Bluetooth True Wireless Apple AirPods Pro Gen 2 USB-C',
    category: 'Phụ kiện',
    originalPrice: 6190000,
    salePrice: 5390000,
    discountPercent: 12,
    imageUrl: 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 540,
    stockCount: 60,
    description: 'Chip H2 mạnh mẽ, tính năng Khử tiếng ồn chủ động gấp 2 lần, cổng sạc USB-C hiện đại.',
    specs: {
      'Chipset': 'Apple H2 headphone chip',
      'Cổng sạc': 'MagSafe USB-C',
      'Chống nước': 'IP54',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-4',
    name: 'Nồi chiên không dầu Philips HD9252/90 4.1 Lít - Công nghệ Rapid Air',
    category: 'Đồ gia dụng',
    originalPrice: 2990000,
    salePrice: 1890000,
    discountPercent: 36,
    imageUrl: 'https://images.unsplash.com/photo-1585515320310-259814833e62?w=500&auto=format&fit=crop&q=60',
    rating: 4.7,
    soldCount: 185,
    stockCount: 19,
    description: 'Giảm 90% lượng chất béo thừa, bảng điều khiển cảm ứng với 7 chương trình nấu cài đặt sẵn.',
    specs: {
      'Dung tích': '4.1 Lít (Rổ chiên 0.8kg)',
      'Công suất': '1400W',
      'Công nghệ': 'Rapid Air luồng khí xoáy',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-5',
    name: 'Bàn phím Cơ Không dây Keychron K2 Pro QMK/VIA Swappable',
    category: 'Phụ kiện',
    originalPrice: 2490000,
    salePrice: 1990000,
    discountPercent: 20,
    imageUrl: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&auto=format&fit=crop&q=60',
    rating: 4.8,
    soldCount: 142,
    stockCount: 22,
    description: 'Bàn phím cơ layout 75% nâng cấp foam tiêu âm, hỗ trợ QMK/VIA tùy chỉnh phím tự do.',
    specs: {
      'Switch': 'Keychron K Pro Red / Brown',
      'Kết nối': 'Bluetooth 5.1 & Type-C Wired',
      'Pin': '4000 mAh',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-6',
    name: 'Đồng hồ Garmin Fenix 7 Pro Sapphire Solar Edition',
    category: 'Đồng hồ',
    originalPrice: 23990000,
    salePrice: 19990000,
    discountPercent: 16,
    imageUrl: 'https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 65,
    stockCount: 12,
    description: 'Đồng hồ thể thao chuyên nghiệp với kính sạc năng lượng mặt trời Power Sapphire, đèn LED chiếu sáng.',
    specs: {
      'Màn hình': '1.3" MIP chống chói',
      'Thời lượng pin': 'Up to 22 ngày (chế độ smartwatch)',
      'Độ bền': 'Chuẩn quân đội MIL-STD-810G',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-7',
    name: 'Máy tính bảng Apple iPad Air 5 M1 10.9 inch Wi-Fi 64GB',
    category: 'Laptop',
    originalPrice: 16990000,
    salePrice: 14490000,
    discountPercent: 14,
    imageUrl: 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 190,
    stockCount: 35,
    description: 'Sức mạnh vượt bậc từ vi xử lý Apple M1, hỗ trợ Apple Pencil 2 và Magic Keyboard biến iPad thành cỗ máy làm việc.',
    specs: {
      'Chipset': 'Apple M1 8-core CPU',
      'RAM / ROM': '8GB / 64GB',
      'Màn hình': '10.9" Liquid Retina True Tone',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-8',
    name: 'Robot hút bụi lau nhà Dreame L10s Ultra Tự động giặt sấy giẻ',
    category: 'Đồ gia dụng',
    originalPrice: 18990000,
    salePrice: 15990000,
    discountPercent: 15,
    imageUrl: 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=500&auto=format&fit=crop&q=60',
    rating: 4.8,
    soldCount: 88,
    stockCount: 16,
    description: 'Trạm sạc đa năng All-in-One tự động hút rác, giặt giẻ, sấy khô bằng khí nóng và tự pha nước lau sàn thông minh.',
    specs: {
      'Lực hút': '5300Pa siêu mạnh',
      'Dung lượng pin': '5200 mAh',
      'Hệ thống điều hướng': 'AI Action + 3D Structured Light',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-9',
    name: 'Chuột Không dây Công thái học Logitech MX Master 3S',
    category: 'Phụ kiện',
    originalPrice: 2690000,
    salePrice: 2290000,
    discountPercent: 15,
    imageUrl: 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 410,
    stockCount: 50,
    description: 'Cảm biến quang học 8000 DPI theo dõi trên mọi bề mặt kể cả kính, nút bấm Silent Clicks êm ái giảm 90% tiếng ồn.',
    specs: {
      'Cảm biến': 'Darkfield 8000 DPI',
      'Cuộn lăn': 'MagSpeed điện từ siêu nhanh',
      'Pin': 'Sạc Type-C dùng tới 70 ngày',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-10',
    name: 'Loa Bluetooth Marshall Stanmore III 80W Âm thanh Hi-Fi',
    category: 'Phụ kiện',
    originalPrice: 10990000,
    salePrice: 9490000,
    discountPercent: 13,
    imageUrl: 'https://images.unsplash.com/photo-1545454675-3531b543be5d?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 96,
    stockCount: 18,
    description: 'Thiết kế Vintage sang trọng đậm chất Rock \'n\' Roll, dải âm trường rộng hơn với công nghệ Dynamic Loudness.',
    specs: {
      'Công suất': '80W Class D Amplifier',
      'Kết nối': 'Bluetooth 5.2, AUX 3.5mm, RCA',
      'Dải tần': '45Hz - 20,000Hz',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-11',
    name: 'Màn hình đồ họa chuyên nghiệp LG UltraFine 27 inch 4K IPS',
    category: 'Phụ kiện',
    originalPrice: 12990000,
    salePrice: 10990000,
    discountPercent: 15,
    imageUrl: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&auto=format&fit=crop&q=60',
    rating: 4.8,
    soldCount: 74,
    stockCount: 20,
    description: 'Độ phân giải 4K UHD sắc nét, chuẩn màu 99% DCI-P3 và hỗ trợ kết nối USB-C sạc ngược 90W tiện lợi cho MacBook.',
    specs: {
      'Kích thước / Tấm nền': '27 inch IPS 4K (3840x2160)',
      'Độ bao phủ màu': '99% DCI-P3, HDR400',
      'Cổng kết nối': 'USB-C 90W, HDMI 2.0, DisplayPort 1.4',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-12',
    name: 'Máy lọc không khí Xiaomi Smart Air Purifier 4 Pro',
    category: 'Đồ gia dụng',
    originalPrice: 5490000,
    salePrice: 4290000,
    discountPercent: 21,
    imageUrl: 'https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=500&auto=format&fit=crop&q=60',
    rating: 4.8,
    soldCount: 310,
    stockCount: 40,
    description: 'Lọc sạch 99.97% bụi mịn PM2.5, phấn hoa và vi khuẩn trong phòng diện tích lên đến 60m², điều khiển qua Mi Home App.',
    specs: {
      'Công suất lọc CADR': '500 m³/h (Khử khuẩn Ion âm)',
      'Diện tích sử dụng': '35 - 60 m²',
      'Độ ồn': '33.7 dB (Chế độ ban đêm)',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-13',
    name: 'Điện thoại Xiaomi 14 Ultra 16GB/512GB Ống kính Leica Summilux',
    category: 'Điện thoại',
    originalPrice: 31990000,
    salePrice: 28990000,
    discountPercent: 9,
    imageUrl: 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 115,
    stockCount: 25,
    description: 'Hệ thống 4 camera Leica 50MP với cảm biến 1 inch thế hệ mới, màn hình 2K WQHD+ 120Hz siêu sáng 3000 nits.',
    specs: {
      'Chipset': 'Snapdragon 8 Gen 3 (4nm)',
      'Camera chính': '50MP 1-inch LYT-900 Variable Aperture',
      'Pin / Sạc': '5000 mAh, Sạc nhanh có dây 90W / Không dây 80W',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-14',
    name: 'Laptop Dell XPS 13 Plus 9320 Core i7-1360P / 16GB / 512GB',
    category: 'Laptop',
    originalPrice: 45990000,
    salePrice: 39990000,
    discountPercent: 13,
    imageUrl: 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=500&auto=format&fit=crop&q=60',
    rating: 4.8,
    soldCount: 52,
    stockCount: 14,
    description: 'Tuyệt tác thiết kế tương lai với bàn di chuột vô hình kính phản hồi xúc giác, bàn phím Zero-lattice tràn viền thanh lịch.',
    specs: {
      'CPU': 'Intel Core i7-1360P (12 cores, 16 threads)',
      'Màn hình': '13.4" 3.5K OLED Touchscreen',
      'Trọng lượng': '1.23 kg siêu nhẹ',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-15',
    name: 'Đồng hồ thể thao chuyên nghiệp Garmin Forerunner 265 Music',
    category: 'Đồng hồ',
    originalPrice: 11690000,
    salePrice: 9990000,
    discountPercent: 14,
    imageUrl: 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 130,
    stockCount: 28,
    description: 'Màn hình AMOLED sắc nét rực rỡ, tích hợp kế hoạch luyện tập chuyên sâu Garmin Coach và GPS đa băng tần chính xác tuyệt đối.',
    specs: {
      'Màn hình': '1.3" AMOLED Always-On',
      'Thời lượng pin': 'Up to 13 ngày (smartwatch), 20 giờ GPS',
      'Bộ nhớ nhạc': 'Lưu trữ tới 500 bài hát offline',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-16',
    name: 'Máy hút bụi không dây Dyson V12 Detect Slim Total Clean',
    category: 'Đồ gia dụng',
    originalPrice: 19990000,
    salePrice: 16990000,
    discountPercent: 15,
    imageUrl: 'https://images.unsplash.com/photo-1558317374-067fb5f30001?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 98,
    stockCount: 15,
    description: 'Công nghệ tia Laser xanh phát hiện hạt bụi siêu nhỏ vô hình, cảm biến Piezo tự động tăng lực hút khi gặp nhiều bụi.',
    specs: {
      'Lực hút': '150 AW',
      'Thời lượng pin': 'Tối đa 60 phút hoạt động liên tục',
      'Trọng lượng': '2.2 kg',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-17',
    name: 'Máy ảnh Mirrorless Sony Alpha A7 IV Body (Chính Hãng)',
    category: 'Phụ kiện',
    originalPrice: 55990000,
    salePrice: 49990000,
    discountPercent: 10,
    imageUrl: 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 40,
    stockCount: 10,
    description: 'Cảm biến Full-frame Exmor R BSI 33MP, bộ xử lý hình ảnh BIONZ XR đỉnh cao, quay phim 4K 60p 10-bit 4:2:2 chuẩn điện ảnh.',
    specs: {
      'Cảm biến': '33MP Full-Frame Exmor R CMOS',
      'Quay video': '4K 60p, 10-bit 4:2:2, S-Cinetone',
      'Lấy nét': '759 điểm lấy nét theo pha Real-time Eye AF',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-18',
    name: 'Máy cạo râu thông minh Philips S5588 Series 5000 SkinIQ',
    category: 'Đồ gia dụng',
    originalPrice: 2390000,
    salePrice: 1890000,
    discountPercent: 20,
    imageUrl: 'https://images.unsplash.com/photo-1621607512214-68297480165e?w=500&auto=format&fit=crop&q=60',
    rating: 4.7,
    soldCount: 220,
    stockCount: 45,
    description: 'Công nghệ cảm biến SkinIQ tự động điều chỉnh tốc độ cắt theo mật độ râu, lưỡi cạo SteelPrecision 45 lưỡi tự mài bén.',
    specs: {
      'Lưỡi dao': 'SteelPrecision 90,000 nhát cắt/phút',
      'Chống nước': '100% Wet & Dry cạo khô hoặc ướt',
      'Pin': 'Sạc 1 giờ dùng 60 phút',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-19',
    name: 'Đồng hồ Samsung Galaxy Watch 6 Classic 47mm Viền xoay',
    category: 'Đồng hồ',
    originalPrice: 9490000,
    salePrice: 7990000,
    discountPercent: 15,
    imageUrl: 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=500&auto=format&fit=crop&q=60',
    rating: 4.8,
    soldCount: 145,
    stockCount: 30,
    description: 'Viền xoay vật lý trứ danh tái xuất, màn hình Sapphire siêu bền, theo dõi thành phần cơ thể BIA và giấc ngủ chuyên sâu.',
    specs: {
      'Màn hình': '1.5" Super AMOLED (480x480) Sapphire Crystal',
      'Khung viền': 'Thép không gỉ cao cấp 47mm',
      'Cảm biến': 'Samsung BioActive (BIA, ECG, HR)',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-20',
    name: 'Bàn chải điện thông minh Oral-B iO Series 9 AI Magnetic',
    category: 'Đồ gia dụng',
    originalPrice: 6490000,
    salePrice: 5490000,
    discountPercent: 15,
    imageUrl: 'https://images.unsplash.com/photo-1559591937-e111a437f14b?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 80,
    stockCount: 25,
    description: 'Công nghệ truyền động từ tính iO mang tính cách mạng kết hợp rung vi mô, màn hình tương tác màu sắc và theo dõi 3D 16 vùng răng.',
    specs: {
      'Chế độ chải': '7 chế độ thông minh (Daily Clean, Whitening, Gum Care...)',
      'Cảm biến lực': 'Smart Pressure Sensor đèn báo 3 màu',
      'Hộp sạc du lịch': 'Power2Go sạc tiện lợi',
    },
    isFlashSale: false,
  },
];

export const productService = {
  /**
   * Lấy danh sách sản phẩm từ Backend Product-Service (MongoDB qua API Gateway)
   */
  async fetchProducts(category?: string, search?: string): Promise<Product[]> {
    try {
      const params = new URLSearchParams();
      if (category && category !== 'Tất cả') {
        params.append('category', category);
      }
      if (search && search.trim() !== '') {
        params.append('search', search.trim());
      }

      const queryString = params.toString() ? `?${params.toString()}` : '';
      const response = await fetch(`/api/v1/products${queryString}`);

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data: any[] = await response.json();
      if (Array.isArray(data) && data.length > 0) {
        return data.map((item) => ({
          id: item.id,
          name: item.name,
          category: item.category || 'Khác',
          description: item.description || '',
          originalPrice: Number(item.price) || 0,
          salePrice: item.discountPrice ? Number(item.price) - Number(item.discountPrice) : Number(item.price) || 0,
          discountPercent: item.discountPercent || (item.discountPrice ? Math.round((Number(item.discountPrice) / Number(item.price)) * 100) : 0),
          imageUrl: item.imageUrl || '',
          rating: item.rating || 4.8,
          soldCount: item.soldCount || 0,
          stockCount: item.stockCount || 50,
          specs: item.specs || {},
          isFlashSale: Boolean(item.isFlashSale),
        }));
      }

      // Nếu database trả về mảng rỗng, áp dụng lọc trên fallback
      return this.filterFallback(category, search);
    } catch (error) {
      console.warn('[ProductService] Backend chưa sẵn sàng, sử dụng dữ liệu cục bộ:', error);
      return this.filterFallback(category, search);
    }
  },

  /**
   * Lấy chi tiết 1 sản phẩm theo ID
   */
  async fetchProductById(id: string): Promise<Product | null> {
    try {
      const response = await fetch(`/api/v1/products/${id}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const item = await response.json();
      return {
        id: item.id,
        name: item.name,
        category: item.category || 'Khác',
        description: item.description || '',
        originalPrice: Number(item.price) || 0,
        salePrice: item.discountPrice ? Number(item.price) - Number(item.discountPrice) : Number(item.price) || 0,
        discountPercent: item.discountPercent || 0,
        imageUrl: item.imageUrl || '',
        rating: item.rating || 4.8,
        soldCount: item.soldCount || 0,
        stockCount: item.stockCount || 50,
        specs: item.specs || {},
        isFlashSale: Boolean(item.isFlashSale),
      };
    } catch {
      return FALLBACK_PRODUCTS.find((p) => p.id === id) || null;
    }
  },

  filterFallback(category?: string, search?: string): Product[] {
    return FALLBACK_PRODUCTS.filter((p) => {
      const matchesCat = !category || category === 'Tất cả' || p.category === category;
      const matchesSearch =
        !search ||
        p.name.toLowerCase().includes(search.toLowerCase()) ||
        p.category.toLowerCase().includes(search.toLowerCase());
      return matchesCat && matchesSearch;
    });
  },
};
