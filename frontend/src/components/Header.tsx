import React, { useState } from 'react';
import { ShoppingCart, Search, User, LogOut, Flame, ShieldCheck, Tag, Menu } from 'lucide-react';
import { useCartStore } from '../store/useCartStore';
import keycloak from '../auth/keycloak';

interface HeaderProps {
  onSearch?: (term: string) => void;
  activeCategory: string;
  onSelectCategory: (category: string) => void;
}

export const Header: React.FC<HeaderProps> = ({ onSearch, activeCategory, onSelectCategory }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const { toggleCart, getTotalCount } = useCartStore();
  const totalCartItems = getTotalCount();

  const categories = [
    'Tất cả',
    'Điện thoại',
    'Laptop',
    'Phụ kiện',
    'Đồng hồ',
    'Đồ gia dụng',
  ];

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (onSearch) {
      onSearch(searchTerm);
    }
  };

  const username = keycloak.tokenParsed?.preferred_username || keycloak.tokenParsed?.name || 'Khách hàng';

  return (
    <header className="sticky top-0 z-40 bg-[#1A94FF] text-white shadow-sm border-b border-blue-600">
      {/* Top Banner Bar */}
      <div className="bg-[#0074DA] text-xs py-1.5 px-4 text-center font-medium flex justify-between items-center max-w-7xl mx-auto">
        <div className="flex items-center gap-2 text-white">
          <Flame className="w-4 h-4 text-yellow-300 animate-pulse" />
          <span>PHIÊN FLASH SALE ĐANG MỞ BÁN - SỐ LƯỢNG CÓ HẠN</span>
        </div>
        <div className="flex items-center gap-4 text-blue-100 hidden md:flex">
          <span className="flex items-center gap-1"><ShieldCheck className="w-3.5 h-3.5" /> 100% Hàng Chính Hãng</span>
          <span className="flex items-center gap-1"><Tag className="w-3.5 h-3.5" /> Giá tốt nhất hôm nay</span>
        </div>
      </div>

      {/* Main Header Content */}
      <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between gap-6">
        {/* Brand Logo */}
        <div className="flex items-center gap-2 cursor-pointer select-none shrink-0">
          <div className="bg-white text-[#1A94FF] p-1.5 rounded-md font-extrabold text-2xl tracking-tight flex items-center gap-1">
            <span>FLSALE</span>
          </div>
          <span className="text-xs text-blue-100 hidden sm:block border-l border-blue-400 pl-3 leading-snug">
            Tốt & Nhanh
          </span>
        </div>

        {/* Search Bar */}
        <form onSubmit={handleSearchSubmit} className="flex-1 max-w-2xl relative flex items-center">
          <input
            type="text"
            placeholder="Tìm sản phẩm, danh mục hay thương hiệu mong muốn..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-white text-slate-800 placeholder-slate-400 text-sm px-4 py-2.5 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-300 transition-shadow shadow-sm"
          />
          <button
            type="submit"
            className="absolute right-1 top-1 bottom-1 px-4 bg-[#0A68FF] hover:bg-[#0055D4] text-white rounded-[4px] flex items-center justify-center transition-colors"
          >
            <Search className="w-4 h-4" />
            <span className="ml-1.5 text-xs font-semibold hidden sm:inline">Tìm kiếm</span>
          </button>
        </form>

        {/* Right Actions: Auth & Cart */}
        <div className="flex items-center gap-4 shrink-0">
          {keycloak.authenticated ? (
            <div className="flex items-center gap-2 bg-blue-600/30 px-3 py-1.5 rounded-md hover:bg-blue-600/50 transition-colors cursor-pointer">
              <User className="w-5 h-5 text-white" />
              <div className="flex flex-col items-start hidden sm:flex">
                <span className="text-[11px] text-blue-100">Tài khoản</span>
                <span className="text-sm font-semibold text-white truncate max-w-[120px] leading-tight">
                  {username}
                </span>
              </div>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  keycloak.logout();
                }}
                title="Đăng xuất"
                className="ml-1 text-blue-200 hover:text-white transition-colors p-1"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <button
              onClick={() => keycloak.login()}
              className="flex items-center gap-2 bg-blue-600/30 px-3 py-1.5 rounded-md hover:bg-blue-600/50 transition-colors"
            >
              <User className="w-5 h-5 text-white" />
              <div className="flex flex-col items-start hidden sm:flex">
                <span className="text-[11px] text-blue-100">Đăng nhập / Đăng ký</span>
                <span className="text-sm font-semibold text-white leading-tight">Tài khoản</span>
              </div>
            </button>
          )}

          {/* Cart Button with Badge */}
          <button
            onClick={toggleCart}
            className="relative flex items-center gap-2 hover:bg-blue-600/50 p-2 rounded-md transition-colors"
            aria-label="Giỏ hàng"
          >
            <div className="relative">
              <ShoppingCart className="w-6 h-6 text-white" />
              {totalCartItems > 0 && (
                <span className="absolute -top-2 -right-2 bg-yellow-400 text-slate-900 text-[10px] font-extrabold w-4 h-4 rounded-[4px] flex items-center justify-center">
                  {totalCartItems > 99 ? '99+' : totalCartItems}
                </span>
              )}
            </div>
            <span className="text-sm font-semibold hidden lg:block">Giỏ hàng</span>
          </button>
        </div>
      </div>

      {/* Category Navigation Bar */}
      <div className="bg-[#1A94FF] px-4 pb-2">
        <div className="max-w-7xl mx-auto flex items-center gap-4 overflow-x-auto scrollbar-none">
          <div className="flex items-center gap-1 text-white font-medium text-sm mr-2 shrink-0 cursor-pointer">
            <Menu className="w-4 h-4" />
            <span>Danh mục sản phẩm</span>
          </div>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => onSelectCategory(cat)}
              className={`px-3 py-1 text-xs font-medium whitespace-nowrap transition-colors rounded-md ${
                activeCategory === cat
                  ? 'bg-white text-[#1A94FF] font-bold'
                  : 'text-blue-50 hover:bg-blue-600'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>
    </header>
  );
};
