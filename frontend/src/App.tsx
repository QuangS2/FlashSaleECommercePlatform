import React, { useState } from 'react';
import keycloak from './auth/keycloak';

export function App() {
  const [activeTab, setActiveTab] = useState<'flashsale' | 'catalog'>('flashsale');

  const handleLogin = () => {
    keycloak.login();
  };

  const handleLogout = () => {
    keycloak.logout();
  };

  return (
    <div style={{ backgroundColor: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif', color: '#0f172a' }}>
      {/* Top Header Bar */}
      <header style={{ backgroundColor: '#0f172a', color: '#ffffff', padding: '12px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ backgroundColor: '#e11d48', padding: '6px 12px', borderRadius: '4px', fontWeight: 'bold', fontSize: '18px', letterSpacing: '0.5px' }}>
            ⚡ FLASH SALE
          </div>
          <span style={{ fontSize: '15px', color: '#94a3b8' }}>Hệ thống Thương mại Điện tử Tải cao</span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          {keycloak.authenticated ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <span style={{ fontSize: '14px', color: '#cbd5e1' }}>
                👤 {keycloak.tokenParsed?.preferred_username || 'Khách hàng'}
              </span>
              <button 
                onClick={handleLogout}
                style={{ backgroundColor: '#334155', color: '#ffffff', border: '1px solid #475569', padding: '6px 14px', borderRadius: '4px', fontSize: '13px', cursor: 'pointer' }}
              >
                Đăng xuất
              </button>
            </div>
          ) : (
            <button 
              onClick={handleLogin}
              style={{ backgroundColor: '#e11d48', color: '#ffffff', border: 'none', padding: '8px 18px', borderRadius: '4px', fontSize: '14px', fontWeight: 600, cursor: 'pointer' }}
            >
              Đăng nhập (Keycloak PKCE)
            </button>
          )}
        </div>
      </header>

      {/* Navigation Banner */}
      <div style={{ backgroundColor: '#ffffff', borderBottom: '1px solid #e2e8f0', padding: '0 24px' }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto', display: 'flex', gap: '24px' }}>
          <button 
            onClick={() => setActiveTab('flashsale')}
            style={{ 
              padding: '14px 16px', 
              border: 'none', 
              background: 'none', 
              fontSize: '15px', 
              fontWeight: activeTab === 'flashsale' ? 600 : 400,
              color: activeTab === 'flashsale' ? '#e11d48' : '#64748b',
              borderBottom: activeTab === 'flashsale' ? '3px solid #e11d48' : '3px solid transparent',
              cursor: 'pointer'
            }}
          >
            🔥 Khung Giờ Flash Sale (09:00 - 12:00)
          </button>
          <button 
            onClick={() => setActiveTab('catalog')}
            style={{ 
              padding: '14px 16px', 
              border: 'none', 
              background: 'none', 
              fontSize: '15px', 
              fontWeight: activeTab === 'catalog' ? 600 : 400,
              color: activeTab === 'catalog' ? '#e11d48' : '#64748b',
              borderBottom: activeTab === 'catalog' ? '3px solid #e11d48' : '3px solid transparent',
              cursor: 'pointer'
            }}
          >
            📦 Danh Mục Sản Phẩm
          </button>
        </div>
      </div>

      {/* Main Container */}
      <main style={{ maxWidth: '1200px', margin: '24px auto', padding: '0 16px' }}>
        
        {/* Flash Sale Banner Block */}
        <div style={{ backgroundColor: '#ffffff', borderRadius: '6px', border: '1px solid #e2e8f0', padding: '20px', marginBottom: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <h2 style={{ fontSize: '20px', margin: 0, color: '#0f172a' }}>SẢN PHẨM GIÁ SỐC ĐANG MỞ BÁN</h2>
              <span style={{ backgroundColor: '#fff1f2', color: '#e11d48', padding: '4px 8px', borderRadius: '4px', fontSize: '12px', fontWeight: 600 }}>
                Giới hạn 1 sản phẩm / tài khoản
              </span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#64748b', fontSize: '14px' }}>
              <span>Kết thúc trong:</span>
              <span style={{ backgroundColor: '#0f172a', color: '#ffffff', padding: '4px 8px', borderRadius: '4px', fontWeight: 'bold' }}>01</span> :
              <span style={{ backgroundColor: '#0f172a', color: '#ffffff', padding: '4px 8px', borderRadius: '4px', fontWeight: 'bold' }}>42</span> :
              <span style={{ backgroundColor: '#0f172a', color: '#ffffff', padding: '4px 8px', borderRadius: '4px', fontWeight: 'bold' }}>18</span>
            </div>
          </div>

          {/* Product Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '20px' }}>
            
            {/* Product Card 1 */}
            <div style={{ backgroundColor: '#ffffff', border: '1px solid #e2e8f0', borderRadius: '6px', overflow: 'hidden', padding: '16px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <div style={{ backgroundColor: '#f1f5f9', height: '160px', borderRadius: '4px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94a3b8', fontSize: '14px', marginBottom: '12px' }}>
                  📱 iPhone 15 Pro Max 256GB
                </div>
                <h3 style={{ fontSize: '15px', fontWeight: 600, color: '#1e293b', margin: '0 0 8px 0', lineHeight: 1.4 }}>
                  iPhone 15 Pro Max 256GB - Chính Hãng VN/A
                </h3>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', marginBottom: '12px' }}>
                  <span style={{ fontSize: '20px', fontWeight: 'bold', color: '#e11d48' }}>24.990.000 đ</span>
                  <span style={{ fontSize: '13px', color: '#94a3b8', textDecoration: 'line-through' }}>29.990.000 đ</span>
                </div>
              </div>

              <div>
                <div style={{ backgroundColor: '#f1f5f9', borderRadius: '4px', height: '14px', overflow: 'hidden', marginBottom: '12px', position: 'relative' }}>
                  <div style={{ backgroundColor: '#e11d48', width: '75%', height: '100%' }}></div>
                  <span style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, fontSize: '10px', color: '#ffffff', textAlign: 'center', lineHeight: '14px', fontWeight: 600 }}>
                    ĐÃ BÁN 75/100
                  </span>
                </div>
                <button 
                  onClick={handleLogin}
                  style={{ width: '100%', backgroundColor: '#0f172a', color: '#ffffff', border: 'none', padding: '10px', borderRadius: '4px', fontWeight: 600, cursor: 'pointer', fontSize: '14px' }}
                >
                  MUA NGAY (FLASH SALE)
                </button>
              </div>
            </div>

            {/* Product Card 2 */}
            <div style={{ backgroundColor: '#ffffff', border: '1px solid #e2e8f0', borderRadius: '6px', overflow: 'hidden', padding: '16px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <div style={{ backgroundColor: '#f1f5f9', height: '160px', borderRadius: '4px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94a3b8', fontSize: '14px', marginBottom: '12px' }}>
                  💻 MacBook Pro M3 14"
                </div>
                <h3 style={{ fontSize: '15px', fontWeight: 600, color: '#1e293b', margin: '0 0 8px 0', lineHeight: 1.4 }}>
                  MacBook Pro 14" M3 (8GB RAM / 512GB SSD)
                </h3>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', marginBottom: '12px' }}>
                  <span style={{ fontSize: '20px', fontWeight: 'bold', color: '#e11d48' }}>35.490.000 đ</span>
                  <span style={{ fontSize: '13px', color: '#94a3b8', textDecoration: 'line-through' }}>39.990.000 đ</span>
                </div>
              </div>

              <div>
                <div style={{ backgroundColor: '#f1f5f9', borderRadius: '4px', height: '14px', overflow: 'hidden', marginBottom: '12px', position: 'relative' }}>
                  <div style={{ backgroundColor: '#e11d48', width: '40%', height: '100%' }}></div>
                  <span style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, fontSize: '10px', color: '#ffffff', textAlign: 'center', lineHeight: '14px', fontWeight: 600 }}>
                    ĐÃ BÁN 20/50
                  </span>
                </div>
                <button 
                  onClick={handleLogin}
                  style={{ width: '100%', backgroundColor: '#0f172a', color: '#ffffff', border: 'none', padding: '10px', borderRadius: '4px', fontWeight: 600, cursor: 'pointer', fontSize: '14px' }}
                >
                  MUA NGAY (FLASH SALE)
                </button>
              </div>
            </div>

          </div>
        </div>
      </main>
    </div>
  );
}

export default App;
