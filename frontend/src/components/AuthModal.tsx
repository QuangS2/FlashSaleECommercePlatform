import React, { useState } from 'react';
import {
  X,
  User,
  Lock,
  Mail,
  Phone,
  Eye,
  EyeOff,
  Sparkles,
  ShieldCheck,
  LogIn,
  UserPlus,
  AlertCircle,
  Loader2,
} from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';

export const AuthModal: React.FC = () => {
  const {
    isAuthModalOpen,
    authModalTab,
    isLoading,
    error,
    closeAuthModal,
    openAuthModal,
    login,
    register,
    loginAsDemo,
    clearError,
  } = useAuthStore();

  // Login form state
  const [loginUsername, setLoginUsername] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [showLoginPassword, setShowLoginPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);

  // Register form state
  const [regFullName, setRegFullName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regUsername, setRegUsername] = useState('');
  const [regPhone, setRegPhone] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regConfirmPassword, setRegConfirmPassword] = useState('');
  const [showRegPassword, setShowRegPassword] = useState(false);
  const [localValidationErr, setLocalValidationErr] = useState<string | null>(null);

  if (!isAuthModalOpen) return null;

  const handleTabChange = (tab: 'login' | 'register') => {
    clearError();
    setLocalValidationErr(null);
    openAuthModal(tab);
  };

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLocalValidationErr(null);

    if (!loginUsername.trim()) {
      setLocalValidationErr('Vui lòng nhập tên đăng nhập hoặc email.');
      return;
    }
    if (!loginPassword.trim()) {
      setLocalValidationErr('Vui lòng nhập mật khẩu.');
      return;
    }

    await login({
      username: loginUsername.trim(),
      password: loginPassword,
      rememberMe,
    });
  };

  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLocalValidationErr(null);

    if (!regFullName.trim()) {
      setLocalValidationErr('Vui lòng nhập họ và tên.');
      return;
    }
    if (!regEmail.trim() || !regEmail.includes('@')) {
      setLocalValidationErr('Vui lòng nhập địa chỉ email hợp lệ.');
      return;
    }
    if (!regUsername.trim() || regUsername.trim().length < 3) {
      setLocalValidationErr('Tên đăng nhập phải từ 3 ký tự trở lên.');
      return;
    }
    if (!regPassword || regPassword.length < 6) {
      setLocalValidationErr('Mật khẩu phải có độ dài từ 6 ký tự trở lên.');
      return;
    }
    if (regPassword !== regConfirmPassword) {
      setLocalValidationErr('Mật khẩu xác nhận không trùng khớp.');
      return;
    }

    await register({
      fullName: regFullName.trim(),
      email: regEmail.trim(),
      username: regUsername.trim(),
      password: regPassword,
      phone: regPhone.trim(),
    });
  };

  const currentError = localValidationErr || error;

  return (
    <div
      className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in duration-200"
      onClick={closeAuthModal}
    >
      <div
        className="bg-white w-full max-w-md rounded-md shadow-2xl overflow-hidden border border-slate-200"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header Tabs */}
        <div className="bg-slate-900 text-white p-4 flex items-center justify-between border-b border-slate-800">
          <div className="flex items-center gap-2">
            <ShieldCheck className="w-5 h-5 text-[#1A94FF]" />
            <h2 className="text-sm font-bold tracking-wide uppercase">
              {authModalTab === 'login' ? 'Đăng Nhập Tài Khoản' : 'Đăng Ký Thành Viên'}
            </h2>
          </div>
          <button
            onClick={closeAuthModal}
            className="text-slate-400 hover:text-white p-1 rounded-md transition-colors"
            aria-label="Đóng"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Selection */}
        <div className="flex border-b border-slate-200 bg-slate-50 text-xs font-bold">
          <button
            type="button"
            onClick={() => handleTabChange('login')}
            className={`flex-1 py-3 text-center border-b-2 transition-colors flex items-center justify-center gap-1.5 ${
              authModalTab === 'login'
                ? 'border-[#1A94FF] text-[#1A94FF] bg-white font-extrabold'
                : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}
          >
            <LogIn className="w-4 h-4" />
            <span>ĐĂNG NHẬP</span>
          </button>
          <button
            type="button"
            onClick={() => handleTabChange('register')}
            className={`flex-1 py-3 text-center border-b-2 transition-colors flex items-center justify-center gap-1.5 ${
              authModalTab === 'register'
                ? 'border-[#1A94FF] text-[#1A94FF] bg-white font-extrabold'
                : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}
          >
            <UserPlus className="w-4 h-4" />
            <span>ĐĂNG KÝ MỚI</span>
          </button>
        </div>

        <div className="p-6 space-y-4">
          {/* Error Alert */}
          {currentError && (
            <div className="bg-rose-50 border border-rose-200 text-rose-700 px-3.5 py-2.5 rounded-md text-xs flex items-start gap-2 animate-in fade-in">
              <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
              <span>{currentError}</span>
            </div>
          )}

          {/* TAB 1: LOGIN FORM */}
          {authModalTab === 'login' ? (
            <form onSubmit={handleLoginSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Tên đăng nhập hoặc Email <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <User className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                  <input
                    type="text"
                    value={loginUsername}
                    onChange={(e) => setLoginUsername(e.target.value)}
                    placeholder="Ví dụ: khachhang_demo hoặc email..."
                    className="w-full text-xs pl-9 pr-3 py-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-[#1A94FF] focus:ring-1 focus:ring-[#1A94FF]"
                    autoFocus
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Mật khẩu <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                  <input
                    type={showLoginPassword ? 'text' : 'password'}
                    value={loginPassword}
                    onChange={(e) => setLoginPassword(e.target.value)}
                    placeholder="Nhập mật khẩu..."
                    className="w-full text-xs pl-9 pr-10 py-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-[#1A94FF] focus:ring-1 focus:ring-[#1A94FF]"
                  />
                  <button
                    type="button"
                    onClick={() => setShowLoginPassword(!showLoginPassword)}
                    className="absolute right-3 top-2.5 text-slate-400 hover:text-slate-600 p-0.5"
                    aria-label="Ẩn hiện mật khẩu"
                  >
                    {showLoginPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              <div className="flex items-center justify-between text-xs pt-1">
                <label className="flex items-center gap-2 cursor-pointer select-none text-slate-600">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    className="rounded border-slate-300 text-[#1A94FF] focus:ring-[#1A94FF]"
                  />
                  <span>Ghi nhớ đăng nhập</span>
                </label>
                <button
                  type="button"
                  onClick={() => alert('Mật khẩu mẫu: Bất kỳ mật khẩu nào từ 3 ký tự trở lên (hoặc dùng 1-Click Demo).')}
                  className="text-[#1A94FF] hover:underline font-medium"
                >
                  Quên mật khẩu?
                </button>
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full bg-[#1A94FF] hover:bg-[#0074DA] text-white font-bold text-xs py-3 rounded-md shadow-sm transition-colors flex items-center justify-center gap-2 disabled:opacity-60"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>ĐANG ĐĂNG NHẬP...</span>
                  </>
                ) : (
                  <>
                    <LogIn className="w-4 h-4" />
                    <span>ĐĂNG NHẬP NGAY</span>
                  </>
                )}
              </button>

              {/* 1-CLICK DEMO ACCESS BOX */}
              <div className="pt-3 border-t border-slate-200">
                <div className="flex items-center gap-1 text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-2">
                  <Sparkles className="w-3.5 h-3.5 text-amber-500" />
                  <span>Trải nghiệm nhanh (1-Click Demo)</span>
                </div>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    type="button"
                    onClick={() => loginAsDemo('CUSTOMER')}
                    className="p-2 bg-blue-50 hover:bg-blue-100 text-[#0074DA] border border-blue-200 rounded-md text-left transition-colors"
                  >
                    <div className="text-xs font-bold flex items-center gap-1">
                      <User className="w-3.5 h-3.5" />
                      <span>Khách Hàng</span>
                    </div>
                    <p className="text-[10px] text-slate-500 mt-0.5">Lê Văn Khách (Demo)</p>
                  </button>

                  <button
                    type="button"
                    onClick={() => loginAsDemo('ADMIN')}
                    className="p-2 bg-slate-100 hover:bg-slate-200 text-slate-800 border border-slate-300 rounded-md text-left transition-colors"
                  >
                    <div className="text-xs font-bold flex items-center gap-1">
                      <ShieldCheck className="w-3.5 h-3.5 text-indigo-600" />
                      <span>Quản Trị Viên</span>
                    </div>
                    <p className="text-[10px] text-slate-500 mt-0.5">Admin System</p>
                  </button>
                </div>
              </div>
            </form>
          ) : (
            /* TAB 2: REGISTER FORM */
            <form onSubmit={handleRegisterSubmit} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Họ và tên <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <User className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                  <input
                    type="text"
                    value={regFullName}
                    onChange={(e) => setRegFullName(e.target.value)}
                    placeholder="Ví dụ: Nguyễn Văn An"
                    className="w-full text-xs pl-9 pr-3 py-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-[#1A94FF] focus:ring-1 focus:ring-[#1A94FF]"
                    autoFocus
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Email <span className="text-rose-500">*</span>
                  </label>
                  <div className="relative">
                    <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                    <input
                      type="email"
                      value={regEmail}
                      onChange={(e) => setRegEmail(e.target.value)}
                      placeholder="an.nguyen@email.com"
                      className="w-full text-xs pl-9 pr-3 py-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-[#1A94FF] focus:ring-1 focus:ring-[#1A94FF]"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Số điện thoại
                  </label>
                  <div className="relative">
                    <Phone className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                    <input
                      type="tel"
                      value={regPhone}
                      onChange={(e) => setRegPhone(e.target.value)}
                      placeholder="0987xxxxxx"
                      className="w-full text-xs pl-9 pr-3 py-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-[#1A94FF] focus:ring-1 focus:ring-[#1A94FF]"
                    />
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Tên đăng nhập <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <User className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                  <input
                    type="text"
                    value={regUsername}
                    onChange={(e) => setRegUsername(e.target.value)}
                    placeholder="nguyenvanan (viết liền không dấu)"
                    className="w-full text-xs pl-9 pr-3 py-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-[#1A94FF] focus:ring-1 focus:ring-[#1A94FF]"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Mật khẩu <span className="text-rose-500">*</span>
                  </label>
                  <div className="relative">
                    <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                    <input
                      type={showRegPassword ? 'text' : 'password'}
                      value={regPassword}
                      onChange={(e) => setRegPassword(e.target.value)}
                      placeholder="Tối thiểu 6 ký tự..."
                      className="w-full text-xs pl-9 pr-8 py-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-[#1A94FF] focus:ring-1 focus:ring-[#1A94FF]"
                    />
                    <button
                      type="button"
                      onClick={() => setShowRegPassword(!showRegPassword)}
                      className="absolute right-2 top-2.5 text-slate-400 hover:text-slate-600 p-0.5"
                    >
                      {showRegPassword ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                    </button>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Xác nhận mật khẩu <span className="text-rose-500">*</span>
                  </label>
                  <div className="relative">
                    <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                    <input
                      type={showRegPassword ? 'text' : 'password'}
                      value={regConfirmPassword}
                      onChange={(e) => setRegConfirmPassword(e.target.value)}
                      placeholder="Nhập lại mật khẩu..."
                      className="w-full text-xs pl-9 pr-3 py-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-[#1A94FF] focus:ring-1 focus:ring-[#1A94FF]"
                    />
                  </div>
                </div>
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full bg-[#1A94FF] hover:bg-[#0074DA] text-white font-bold text-xs py-3 rounded-md shadow-sm transition-colors flex items-center justify-center gap-2 mt-2 disabled:opacity-60"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>ĐANG XỬ LÝ...</span>
                  </>
                ) : (
                  <>
                    <UserPlus className="w-4 h-4" />
                    <span>TẠO TÀI KHOẢN MỚI</span>
                  </>
                )}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
