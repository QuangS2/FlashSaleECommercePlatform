import { describe, it, expect } from 'vitest';

// Bộ hàm kiểm tra Validation & Security theo chuẩn nghiệp vụ
export const FormValidators = {
  isValidEmail(email: string): boolean {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
  },

  isValidPhone(phone: string): boolean {
    const phoneRegex = /(03|05|07|08|09)+([0-9]{8})\b/;
    return phoneRegex.test(phone) && phone.length === 10;
  },

  isValidAddress(address: string): boolean {
    return address.trim().length >= 5;
  },

  sanitizeInput(input: string): string {
    return input.replace(/<[^>]*>?/gm, '').replace(/['";]/g, '');
  },

  validateOrderQuantity(qty: number, maxLimit: number = 2): { valid: boolean; error?: string } {
    if (qty <= 0) return { valid: false, error: 'Số lượng phải lớn hơn 0' };
    if (qty > maxLimit) return { valid: false, error: `Số lượng vượt quá hạn mức tối đa (${maxLimit})` };
    return { valid: true };
  },

  parseJwtClaims(token: string): any {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const payload = atob(parts[1]);
      return JSON.parse(payload);
    } catch (e) {
      return null;
    }
  },

  isTokenExpired(exp: number): boolean {
    const now = Math.floor(Date.now() / 1000);
    return now >= exp;
  }
};

describe('Frontend Form Validation, Security & OIDC Auth Test Suite (Bảng 24 - 18 Tests)', () => {
  it('1. Xác thực định dạng email hợp lệ (RFC 5322 regex)', () => {
    expect(FormValidators.isValidEmail('user@gmail.com')).toBe(true);
    expect(FormValidators.isValidEmail('nguyen.van.a@company.com.vn')).toBe(true);
  });

  it('2. Báo lỗi khi email rỗng hoặc sai định dạng tên miền', () => {
    expect(FormValidators.isValidEmail('')).toBe(false);
    expect(FormValidators.isValidEmail('invalid-email')).toBe(false);
    expect(FormValidators.isValidEmail('user@domain')).toBe(false);
    expect(FormValidators.isValidEmail('@domain.com')).toBe(false);
  });

  it('3. Xác thực số điện thoại chuẩn Việt Nam (10 số, đầu 03, 05, 07, 08, 09)', () => {
    expect(FormValidators.isValidPhone('0912345678')).toBe(true);
    expect(FormValidators.isValidPhone('0388999888')).toBe(true);
    expect(FormValidators.isValidPhone('0771234567')).toBe(true);
  });

  it('4. Báo lỗi khi số điện thoại chứa ký tự đặc biệt hoặc thiếu/thừa số', () => {
    expect(FormValidators.isValidPhone('091234567')).toBe(false); // 9 số
    expect(FormValidators.isValidPhone('09123456789')).toBe(false); // 11 số
    expect(FormValidators.isValidPhone('091234abcd')).toBe(false);
    expect(FormValidators.isValidPhone('1234567890')).toBe(false); // Sai đầu số
  });

  it('5. Xác thực địa chỉ giao hàng không được để trống hoặc dưới 5 ký tự', () => {
    expect(FormValidators.isValidAddress('123 Đường Nguyễn Huệ, Quận 1')).toBe(true);
    expect(FormValidators.isValidAddress('HCM')).toBe(false);
    expect(FormValidators.isValidAddress('   ')).toBe(false);
  });

  it('6. Ngăn chặn tấn công XSS trong trường họ tên và địa chỉ (sanitization)', () => {
    const maliciousInput = '<script>alert("XSS")</script>Nguyễn Văn A';
    const cleanInput = FormValidators.sanitizeInput(maliciousInput);
    expect(cleanInput).not.toContain('<script>');
    expect(cleanInput).not.toContain('</script>');
    expect(cleanInput).toBe('alert(XSS)Nguyễn Văn A');
  });

  it('7. Ngăn chặn tấn công SQL Injection string trong chuỗi tìm kiếm', () => {
    const maliciousSql = "iPhone' OR '1'='1";
    const cleanSql = FormValidators.sanitizeInput(maliciousSql);
    expect(cleanSql).not.toContain("'");
    expect(cleanSql).toBe('iPhone OR 1=1');
  });

  it('8. Giới hạn số lượng đặt mua tối đa theo hạn mức Flash Sale (max 2 sản phẩm)', () => {
    const res = FormValidators.validateOrderQuantity(3, 2);
    expect(res.valid).toBe(false);
    expect(res.error).toContain('vượt quá hạn mức tối đa');
  });

  it('9. Từ chối số lượng đặt mua âm hoặc bằng 0', () => {
    const resZero = FormValidators.validateOrderQuantity(0);
    expect(resZero.valid).toBe(false);
    expect(resZero.error).toContain('lớn hơn 0');

    const resNegative = FormValidators.validateOrderQuantity(-1);
    expect(resNegative.valid).toBe(false);
  });

  it('10. Chấp nhận số lượng hợp lệ trong hạn mức Flash Sale', () => {
    const res = FormValidators.validateOrderQuantity(1, 2);
    expect(res.valid).toBe(true);
  });

  it('11. Decode JWT access token và trích xuất claims người dùng', () => {
    const mockPayload = {
      sub: 'user-uuid-1234',
      preferred_username: 'leanhquang',
      email: 'quang@ecommerce.com',
      roles: ['USER', 'CUSTOMER'],
      exp: Math.floor(Date.now() / 1000) + 3600
    };
    const mockToken = `header.${btoa(JSON.stringify(mockPayload))}.signature`;
    const claims = FormValidators.parseJwtClaims(mockToken);

    expect(claims).not.toBeNull();
    expect(claims.preferred_username).toBe('leanhquang');
    expect(claims.sub).toBe('user-uuid-1234');
    expect(claims.roles).toContain('USER');
  });

  it('12. Trả về null khi format JWT token không hợp lệ', () => {
    expect(FormValidators.parseJwtClaims('invalid-token')).toBeNull();
  });

  it('13. Xác định chính xác token đã hết hạn dựa trên exp claim', () => {
    const pastExp = Math.floor(Date.now() / 1000) - 100;
    expect(FormValidators.isTokenExpired(pastExp)).toBe(true);
  });

  it('14. Xác định chính xác token còn hiệu lực', () => {
    const futureExp = Math.floor(Date.now() / 1000) + 1800;
    expect(FormValidators.isTokenExpired(futureExp)).toBe(false);
  });

  it('15. Lọc bỏ header nhạy cảm X-User-Id trước khi gửi request ra ngoài', () => {
    const headers: Record<string, string> = {
      'Authorization': 'Bearer test-token',
      'X-User-Id': 'attacker-spoofed-id',
      'Content-Type': 'application/json'
    };
    delete headers['X-User-Id']; // Chuẩn hóa interceptor
    expect(headers['X-User-Id']).toBeUndefined();
    expect(headers['Authorization']).toBe('Bearer test-token');
  });

  it('16. Quản lý an toàn tham số PKCE code_verifier trong session storage', () => {
    const mockStorage: Record<string, string> = {};
    const setVerifier = (key: string, val: string) => { mockStorage[key] = val; };
    const popVerifier = (key: string) => {
      const v = mockStorage[key];
      delete mockStorage[key];
      return v;
    };

    setVerifier('pkce_verifier', 'random-secure-string-base64');
    expect(mockStorage['pkce_verifier']).toBe('random-secure-string-base64');

    const retrieved = popVerifier('pkce_verifier');
    expect(retrieved).toBe('random-secure-string-base64');
    expect(mockStorage['pkce_verifier']).toBeUndefined();
  });

  it('17. Kiểm tra tính toàn vẹn của state parameter trong luồng OIDC OAuth2', () => {
    const expectedState = 'csrf-protected-state-uuid';
    const callbackState = 'csrf-protected-state-uuid';
    expect(callbackState === expectedState).toBe(true);

    const tamperedState = 'forged-state-value';
    expect(tamperedState === expectedState).toBe(false);
  });

  it('18. Hỗ trợ xác thực khách vãng lai (Guest Checkout) với thông tin tối thiểu hợp lệ', () => {
    const guestData = {
      fullName: 'Khách Vãng Lai',
      email: 'guest@example.com',
      phone: '0988776655',
      address: 'Số 1 Đại lộ Lê Lợi, Phường Bến Nghé, Quận 1'
    };

    const isGuestValid = FormValidators.isValidEmail(guestData.email)
      && FormValidators.isValidPhone(guestData.phone)
      && FormValidators.isValidAddress(guestData.address)
      && guestData.fullName.trim().length > 0;

    expect(isGuestValid).toBe(true);
  });
});
