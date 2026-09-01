import React from 'react';
import { X, ShieldCheck, User, KeyRound, ExternalLink, Sparkles, CheckCircle2, Info } from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';

export const LoginModal: React.FC = () => {
  const { isLoginModalOpen, closeLoginModal, loginWithKeycloak, demoLogin } = useAuthStore();

  if (!isLoginModalOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 animate-fade-in">
      <div className="bg-white rounded-lg shadow-2xl max-w-md w-full overflow-hidden border border-slate-200 animate-scale-up">
        {/* Modal Header */}
        <div className="bg-slate-900 text-white p-5 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="bg-[#1A94FF] p-1.5 rounded text-white font-extrabold text-lg tracking-tight">
              FLSALE
            </div>
            <div>
              <h3 className="font-bold text-base leading-tight">Đăng Nhập Tài Khoản</h3>
              <p className="text-xs text-slate-300">Đồng bộ giỏ hàng và lịch sử đơn hàng</p>
            </div>
          </div>
          <button
            onClick={closeLoginModal}
            className="text-slate-400 hover:text-white p-1 rounded transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-6 space-y-5">
          {/* Primary Keycloak Login Button */}
          <div className="space-y-3">
            <button
              onClick={loginWithKeycloak}
              className="w-full bg-[#1A94FF] hover:bg-[#0074DA] text-white font-bold py-3.5 px-4 rounded-md flex items-center justify-center gap-2.5 text-sm shadow-md hover:shadow-lg transition-all transform active:scale-[0.99]"
            >
              <KeyRound className="w-4 h-4" />
              <span>Đăng Nhập Bằng Tài Khoản & Mật Khẩu</span>
              <ExternalLink className="w-4 h-4 ml-1 opacity-80" />
            </button>

            {/* Sample Credentials Box */}
            <div className="bg-blue-50/70 border border-blue-200 rounded-md p-3 text-xs text-slate-700 space-y-2">
              <div className="flex items-center gap-1.5 font-semibold text-blue-900">
                <Info className="w-3.5 h-3.5 text-[#1A94FF]" />
                <span>Tài khoản & Mật khẩu mẫu đã cấp sẵn:</span>
              </div>
              <div className="grid grid-cols-2 gap-2 text-[11px]">
                <div className="bg-white p-2 rounded border border-blue-100 shadow-xs">
                  <div className="font-bold text-slate-800">Khách hàng:</div>
                  <div className="text-slate-600">User: <code className="text-blue-700 font-mono font-semibold">customer</code></div>
                  <div className="text-slate-600">Pass: <code className="text-blue-700 font-mono font-semibold">password</code></div>
                </div>
                <div className="bg-white p-2 rounded border border-blue-100 shadow-xs">
                  <div className="font-bold text-slate-800">Quản trị viên:</div>
                  <div className="text-slate-600">User: <code className="text-blue-700 font-mono font-semibold">admin</code></div>
                  <div className="text-slate-600">Pass: <code className="text-blue-700 font-mono font-semibold">adminpassword</code></div>
                </div>
              </div>
            </div>
          </div>

          <div className="relative flex items-center justify-center">
            <div className="border-t border-slate-200 w-full"></div>
            <span className="bg-white px-3 text-xs font-semibold text-slate-400 shrink-0">HOẶC</span>
          </div>

          {/* Quick 1-Click Demo Login Options */}
          <div className="space-y-2.5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-[#1A94FF]" /> Đăng nhập trải nghiệm nhanh
              </span>
              <span className="text-[11px] text-emerald-600 font-semibold bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                1-Click Active
              </span>
            </div>

            {/* Customer Demo Account */}
            <button
              onClick={() => demoLogin('customer')}
              className="w-full bg-slate-50 hover:bg-blue-50/60 border border-slate-200 hover:border-[#1A94FF] rounded-lg p-3 flex items-center justify-between text-left transition-all group"
            >
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-full bg-blue-100 text-[#1A94FF] flex items-center justify-center font-bold text-sm shrink-0">
                  <User className="w-4 h-4" />
                </div>
                <div>
                  <div className="font-bold text-sm text-slate-800 group-hover:text-[#1A94FF] transition-colors">
                    Tài khoản Khách hàng (Customer)
                  </div>
                  <div className="text-xs text-slate-400">Username: customer | Quyền: ROLE_CUSTOMER</div>
                </div>
              </div>
              <CheckCircle2 className="w-4 h-4 text-slate-300 group-hover:text-[#1A94FF] transition-colors" />
            </button>

            {/* Admin Demo Account */}
            <button
              onClick={() => demoLogin('admin')}
              className="w-full bg-slate-50 hover:bg-blue-50/60 border border-slate-200 hover:border-[#1A94FF] rounded-lg p-3 flex items-center justify-between text-left transition-all group"
            >
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center font-bold text-sm shrink-0">
                  <ShieldCheck className="w-4 h-4" />
                </div>
                <div>
                  <div className="font-bold text-sm text-slate-800 group-hover:text-indigo-600 transition-colors">
                    Tài khoản Quản trị (Admin)
                  </div>
                  <div className="text-xs text-slate-400">Username: admin | Quyền: ROLE_ADMIN</div>
                </div>
              </div>
              <CheckCircle2 className="w-4 h-4 text-slate-300 group-hover:text-indigo-600 transition-colors" />
            </button>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="bg-slate-50 p-3.5 border-t border-slate-100 text-center text-xs text-slate-500">
          Chưa có tài khoản?{' '}
          <button
            onClick={loginWithKeycloak}
            className="text-[#1A94FF] font-bold hover:underline"
          >
            Đăng ký tài khoản mới trên Keycloak
          </button>
        </div>
      </div>
    </div>
  );
};
