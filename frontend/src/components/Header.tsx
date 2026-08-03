import React, { useState } from 'react';
import { ShoppingCart, Search, User, LogOut, Flame, ShieldCheck, Tag } from 'lucide-react';
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
    <header className="sticky top-0 z-40 bg-slate-900 text-white shadow-md border-b border-slate-800">
      {/* Top Banner Bar */}
      <div className="bg-gradient-to-r from-rose-700 via-rose-600 to-rose-700 text-xs py-1.5 px-4 text-center font-medium flex justify-between items-center max-w-7xl mx-auto">
        <div className="flex items-center gap-2">
          <Flame className="w-4 h-4 text-amber-300 animate-pulse" />
          <span>PHIÊN FLASH SALE ĐANG MỞ BÁN - SỐ LƯỢNG CÓ HẠN</span>
        </div>
        <div className="flex items-center gap-4 text-rose-100 hidden md:flex">
          <span className="flex items-center gap-1"><ShieldCheck className="w-3.5 h-3.5" /> 100% Hàng Chính Hãng</span>
          <span className="flex items-center gap-1"><Tag className="w-3.5 h-3.5" /> Giá tốt nhất hôm nay</span>
        </div>
      </div>

      {/* Main Header Content */}
      <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between gap-4">
        {/* Brand Logo */}
        <div className="flex items-center gap-3 cursor-pointer select-none">
          <div className="bg-rose-600 text-white p-2 rounded-md font-extrabold text-xl tracking-wider flex items-center gap-1.5 shadow-sm">
            <Flame className="w-6 h-6 text-yellow-300 fill-yellow-300" />
            <span>FLASH SALE</span>
          </div>
          <span className="text-xs text-slate-400 hidden sm:inline border-l border-slate-700 pl-3">
            Mua sắm thông minh, giá cực sốc
          </span>
        </div>

        {/* Search Bar */}
        <form onSubmit={handleSearchSubmit} className="flex-1 max-w-xl relative">
          <input
            type="text"
            placeholder="Tìm kiếm sản phẩm giá rẻ, ưu đãi Flash Sale..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-slate-800 text-slate-100 placeholder-slate-400 text-sm px-4 py-2 pr-10 rounded-md border border-slate-700 focus:outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 transition-colors"
          />
          <button
            type="submit"
            className="absolute right-1 top-1 bottom-1 px-3 bg-rose-600 hover:bg-rose-700 text-white rounded-md flex items-center justify-center transition-colors"
          >
            <Search className="w-4 h-4" />
          </button>
        </form>

        {/* Right Actions: Auth & Cart */}
        <div className="flex items-center gap-3">
          {keycloak.authenticated ? (
            <div className="flex items-center gap-2 bg-slate-800 px-3 py-1.5 rounded-md border border-slate-700">
              <User className="w-4 h-4 text-rose-400" />
              <span className="text-sm font-medium text-slate-200 truncate max-w-[120px]">
                {username}
              </span>
              <button
                onClick={() => keycloak.logout()}
                title="Đăng xuất"
                className="ml-1 text-slate-400 hover:text-rose-400 transition-colors p-1"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <button
              onClick={() => keycloak.login()}
              className="bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold px-4 py-2 rounded-md shadow-sm transition-colors flex items-center gap-1.5"
            >
              <User className="w-4 h-4" />
              <span>Đăng nhập</span>
            </button>
          )}

          {/* Cart Button with Badge */}
          <button
            onClick={toggleCart}
            className="relative bg-slate-800 hover:bg-slate-700 text-slate-100 p-2 rounded-md border border-slate-700 transition-colors flex items-center justify-center"
            aria-label="Giỏ hàng"
          >
            <ShoppingCart className="w-5 h-5 text-rose-400" />
            {totalCartItems > 0 && (
              <span className="absolute -top-1.5 -right-1.5 bg-rose-600 text-white text-[11px] font-bold w-5 h-5 rounded-full flex items-center justify-center border-2 border-slate-900 animate-bounce">
                {totalCartItems > 99 ? '99+' : totalCartItems}
              </span>
            )}
          </button>
        </div>
      </div>

      {/* Category Navigation Bar */}
      <div className="bg-slate-950 border-t border-slate-800/80 px-4">
        <div className="max-w-7xl mx-auto flex items-center gap-1 overflow-x-auto py-1 scrollbar-none">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => onSelectCategory(cat)}
              className={`px-3 py-1.5 text-xs font-medium rounded-md whitespace-nowrap transition-colors ${
                activeCategory === cat
                  ? 'bg-rose-600 text-white font-semibold'
                  : 'text-slate-300 hover:bg-slate-800 hover:text-white'
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
