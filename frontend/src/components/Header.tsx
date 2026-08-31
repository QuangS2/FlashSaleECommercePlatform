import React, { useState } from 'react';
import { ShoppingCart, Search, User, LogOut, Flame, ShieldCheck, Tag, Menu } from 'lucide-react';
import { useCartStore } from '../store/useCartStore';
import keycloak from '../auth/keycloak';

interface HeaderProps {
  onSearch?: (term: string) => void;
  activeCategory: string;
  onSelectCategory: (category: string) => void;
}

export const Header: React.FC<HeaderProps> = ({
  onSearch,
  activeCategory,
  onSelectCategory,
}) => {
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

  const isUserLoggedIn = Boolean(keycloak.authenticated);
  const displayName =
    keycloak.tokenParsed?.name ||
    keycloak.tokenParsed?.preferred_username ||
    'Tài khoản';

  const handleAccountClick = () => {
    if (!isUserLoggedIn) {
      keycloak.login();
    }
  };

  const handleLogoutClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (keycloak.authenticated) {
      keycloak.logout();
    }
  };

  return (
    <header className="sticky top-0 z-40 bg-[#1A94FF] text-white shadow-sm border-b border-blue-600">
      {/* Top Banner Bar */}
      <div className="bg-[#0074DA] text-xs py-1.5 px-4 text-center font-medium flex justify-between items-center max-w-7xl mx-auto">
        <div className="flex items-center gap-2 text-white">
          <Flame className="w-4 h-4 text-yellow-300 animate-pulse" />
          <span>PHIÊN FLASH SALE ĐANG MỞ BÁN - SỐ LƯỢNG CÓ HẠN</span>
        </div>
        <div className="flex items-center gap-4 text-blue-100 hidden md:flex">
          <span className="flex items-center gap-1">
            <ShieldCheck className="w-3.5 h-3.5" /> 100% Hàng Chính Hãng
          </span>
          <span className="flex items-center gap-1">
            <Tag className="w-3.5 h-3.5" /> Giá tốt nhất hôm nay
          </span>
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
            className="absolute right-1.5 bg-[#0074DA] hover:bg-blue-800 text-white px-4 py-1.5 rounded-md text-xs font-semibold flex items-center gap-1.5 transition-colors"
          >
            <Search className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Tìm kiếm</span>
          </button>
        </form>

        {/* User Actions: Account & Cart */}
        <div className="flex items-center gap-3">
          {/* User Account / Keycloak Login */}
          <div
            onClick={handleAccountClick}
            className="flex items-center gap-2 cursor-pointer hover:bg-blue-600/60 px-3 py-1.5 rounded-md transition-colors select-none group"
            title={isUserLoggedIn ? 'Tài khoản của tôi' : 'Đăng nhập với Keycloak IAM'}
          >
            <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center text-white border border-white/30">
              <User className="w-4 h-4" />
            </div>
            <div className="text-left hidden md:block">
              <div className="text-[11px] text-blue-100 leading-tight">
                {isUserLoggedIn ? 'Xin chào,' : 'Tài khoản'}
              </div>
              <div className="text-xs font-semibold leading-tight flex items-center gap-1">
                <span>{displayName}</span>
              </div>
            </div>
            {isUserLoggedIn && (
              <button
                onClick={handleLogoutClick}
                className="ml-1 p-1 hover:bg-red-500/80 rounded transition-colors text-blue-100 hover:text-white"
                title="Đăng xuất khỏi Keycloak"
              >
                <LogOut className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          {/* Cart Icon & Badge */}
          <button
            onClick={toggleCart}
            className="relative flex items-center gap-2 bg-[#0074DA] hover:bg-blue-800 px-3.5 py-2 rounded-md transition-colors shadow-sm text-xs font-semibold"
          >
            <div className="relative">
              <ShoppingCart className="w-5 h-5" />
              {totalCartItems > 0 && (
                <span className="absolute -top-2 -right-2 bg-red-500 text-white text-[10px] font-bold w-4 h-4 rounded-full flex items-center justify-center border-2 border-[#0074DA] animate-scale-in">
                  {totalCartItems > 99 ? '99+' : totalCartItems}
                </span>
              )}
            </div>
            <span className="hidden sm:inline">Giỏ hàng</span>
          </button>
        </div>
      </div>

      {/* Navigation Categories Bar */}
      <div className="bg-white text-slate-700 border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 flex items-center gap-6 overflow-x-auto py-2 scrollbar-none text-xs font-medium">
          <div className="flex items-center gap-1.5 text-[#1A94FF] font-bold pr-2 border-r border-slate-200 shrink-0">
            <Menu className="w-4 h-4" />
            <span>DANH MỤC</span>
          </div>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => onSelectCategory(cat)}
              className={`shrink-0 transition-colors py-0.5 px-2 rounded ${
                activeCategory === cat
                  ? 'text-[#1A94FF] font-bold bg-blue-50'
                  : 'text-slate-600 hover:text-[#1A94FF]'
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
