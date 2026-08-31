import React from 'react';
import { X, User, Mail, Phone, MapPin, ShieldCheck, LogOut, Shield } from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';

interface UserProfileDrawerProps {
  isOpen: boolean;
  onClose: () => void;
}

export const UserProfileDrawer: React.FC<UserProfileDrawerProps> = ({ isOpen, onClose }) => {
  const { user, logout } = useAuthStore();

  if (!isOpen || !user) return null;

  const isAdmin = user.roles.includes('ROLE_ADMIN');

  const handleLogoutClick = () => {
    logout();
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden animate-in fade-in duration-200">
      {/* Overlay */}
      <div
        className="absolute inset-0 bg-slate-900/60 backdrop-blur-xs transition-opacity"
        onClick={onClose}
      />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-sm bg-white shadow-2xl flex flex-col justify-between border-l border-slate-200">
          
          {/* Header */}
          <div>
            <div className="p-4 bg-slate-900 text-white flex items-center justify-between border-b border-slate-800">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-[#1A94FF]" />
                <h2 className="text-sm font-bold tracking-wide uppercase">Thông Tin Tài Khoản</h2>
              </div>
              <button
                onClick={onClose}
                className="text-slate-400 hover:text-white p-1 rounded-md transition-colors"
                aria-label="Đóng"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Profile Avatar Card */}
            <div className="p-6 bg-slate-50 border-b border-slate-200 flex flex-col items-center text-center">
              <div className="w-16 h-16 bg-[#1A94FF] text-white rounded-full flex items-center justify-center font-bold text-xl mb-3 shadow-md border-2 border-white">
                {user.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
              </div>
              <h3 className="text-base font-bold text-slate-900 leading-snug">{user.fullName}</h3>
              <p className="text-xs text-slate-500 font-mono mt-0.5">@{user.username}</p>
              
              <div className="mt-2.5">
                {isAdmin ? (
                  <span className="inline-flex items-center gap-1 bg-indigo-50 text-indigo-700 border border-indigo-200 text-[10px] font-bold px-2.5 py-0.5 rounded-full">
                    <Shield className="w-3 h-3" />
                    <span>QUẢN TRỊ VIÊN (ADMIN)</span>
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1 bg-blue-50 text-[#0074DA] border border-blue-200 text-[10px] font-bold px-2.5 py-0.5 rounded-full">
                    <User className="w-3 h-3" />
                    <span>THÀNH VIÊN KHÁCH HÀNG</span>
                  </span>
                )}
              </div>
            </div>

            {/* Details List */}
            <div className="p-6 space-y-4 text-xs">
              <div>
                <span className="text-slate-400 font-medium block mb-1">Địa chỉ Email:</span>
                <div className="flex items-center gap-2 text-slate-800 font-semibold bg-white p-2.5 rounded-md border border-slate-200">
                  <Mail className="w-4 h-4 text-slate-400 shrink-0" />
                  <span className="truncate">{user.email}</span>
                </div>
              </div>

              <div>
                <span className="text-slate-400 font-medium block mb-1">Số điện thoại:</span>
                <div className="flex items-center gap-2 text-slate-800 font-semibold bg-white p-2.5 rounded-md border border-slate-200">
                  <Phone className="w-4 h-4 text-slate-400 shrink-0" />
                  <span>{user.phone || 'Chưa cập nhật'}</span>
                </div>
              </div>

              <div>
                <span className="text-slate-400 font-medium block mb-1">Địa chỉ giao hàng mặc định:</span>
                <div className="flex items-start gap-2 text-slate-800 font-semibold bg-white p-2.5 rounded-md border border-slate-200">
                  <MapPin className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
                  <span className="leading-relaxed">{user.address || 'Chưa cập nhật địa chỉ giao hàng'}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Footer Action */}
          <div className="p-4 bg-slate-50 border-t border-slate-200">
            <button
              onClick={handleLogoutClick}
              className="w-full bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs py-3 rounded-md shadow-sm transition-colors flex items-center justify-center gap-2"
            >
              <LogOut className="w-4 h-4" />
              <span>ĐĂNG XUẤT TÀI KHOẢN</span>
            </button>
          </div>

        </div>
      </div>
    </div>
  );
};
