<%@page contentType="text/html" pageEncoding="UTF-8"%>

<style>
    body.customer-sidebar-layout {
        min-height: 100vh;
    }

    #customerSidebar {
        width: 18rem;
        transform: translateX(-100%);
        transition: transform 0.2s ease;
    }

    #customerSidebar.open {
        transform: translateX(0);
    }

    #customerSidebarBackdrop {
        display: none;
    }

    #customerSidebarBackdrop.open {
        display: block;
    }

    .customer-nav-link.active {
        background-color: #ecfdf5;
        color: #047857;
        border-color: #a7f3d0;
    }

    @media (min-width: 1024px) {
        body.customer-sidebar-layout {
            padding-left: 18rem;
        }

        #customerSidebar {
            transform: translateX(0);
        }

        #customerSidebarBackdrop {
            display: none !important;
        }

        #customerSidebarToggleBar {
            display: none;
        }
    }
</style>

<div id="customerSidebarToggleBar" class="lg:hidden sticky top-0 z-40 bg-white border-b border-gray-100 px-4 py-3 flex items-center justify-between">
    <button id="customerSidebarToggle" type="button" class="w-10 h-10 rounded-xl border border-gray-200 text-gray-600 hover:text-[#008751] hover:border-[#008751] flex items-center justify-center">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path d="M3 6h18M3 12h18M3 18h18"/>
        </svg>
    </button>
    <a href="${pageContext.request.contextPath}/customer/dashboard" class="flex items-center gap-2">
        <div class="bg-[#008751] p-1.5 rounded-lg">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M8 21h8M12 17v4"/>
                <path d="M7 4h10v4a5 5 0 01-10 0V4z"/>
                <path d="M4 4h3v2a4 4 0 01-3 4"/>
                <path d="M20 4h-3v2a4 4 0 003 4"/>
            </svg>
        </div>
        <span class="text-base font-black tracking-tight text-gray-900">FIFA<span class="text-[#008751]">FIELD</span></span>
    </a>
    <span class="w-10 h-10"></span>
</div>

<div id="customerSidebarBackdrop" class="fixed inset-0 bg-black/30 z-40 lg:hidden"></div>

<aside id="customerSidebar" class="fixed left-0 top-0 h-screen bg-white border-r border-gray-100 z-50 flex flex-col shadow-xl lg:shadow-none">
    <div class="px-6 py-6 border-b border-gray-100">
        <a href="${pageContext.request.contextPath}/customer/dashboard" class="flex items-center gap-3">
            <div class="bg-[#008751] p-2 rounded-xl">
                <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M8 21h8M12 17v4"/>
                    <path d="M7 4h10v4a5 5 0 01-10 0V4z"/>
                    <path d="M4 4h3v2a4 4 0 01-3 4"/>
                    <path d="M20 4h-3v2a4 4 0 003 4"/>
                </svg>
            </div>
            <div>
                <p class="text-xl font-black tracking-tight text-gray-900">FIFA<span class="text-[#008751]">FIELD</span></p>
                <p class="text-[10px] uppercase tracking-[0.2em] text-gray-400 font-bold">Customer Panel</p>
            </div>
        </a>
    </div>

    <nav class="px-4 py-4 space-y-2 overflow-y-auto">
        <a data-nav-key="dashboard" href="${pageContext.request.contextPath}/customer/dashboard" class="customer-nav-link flex items-center gap-3 px-4 py-3 rounded-2xl border border-transparent text-sm font-semibold text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-all">
            <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
                <path d="M9 22V12h6v10"/>
            </svg>
            Bảng điều khiển
        </a>

        <a data-nav-key="locations" href="${pageContext.request.contextPath}/customer/locations" class="customer-nav-link flex items-center gap-3 px-4 py-3 rounded-2xl border border-transparent text-sm font-semibold text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-all">
            <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M12 21s7-6.5 7-11a7 7 0 10-14 0c0 4.5 7 11 7 11z"/>
                <circle cx="12" cy="10" r="3"/>
            </svg>
            Cơ sở
        </a>

        <a data-nav-key="bookings" href="${pageContext.request.contextPath}/customer/bookings" class="customer-nav-link flex items-center gap-3 px-4 py-3 rounded-2xl border border-transparent text-sm font-semibold text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-all">
            <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <path d="M16 2v4M8 2v4M3 10h18"/>
            </svg>
            Lịch sử đặt sân
        </a>

        <a data-nav-key="my-calendar" href="${pageContext.request.contextPath}/customer/my-calendar" class="customer-nav-link flex items-center gap-3 px-4 py-3 rounded-2xl border border-transparent text-sm font-semibold text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-all">
            <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M8 2v4M16 2v4M3 10h18"/>
                <rect x="3" y="4" width="18" height="18" rx="2"/>
                <path d="M8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01M16 18h.01"/>
            </svg>
            Lịch chơi của tôi
        </a>

        <a data-nav-key="blogs" href="${pageContext.request.contextPath}/customer/blogs" class="customer-nav-link flex items-center gap-3 px-4 py-3 rounded-2xl border border-transparent text-sm font-semibold text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-all">
            <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
            </svg>
            Tin tức
        </a>

        <a data-nav-key="vouchers" href="${pageContext.request.contextPath}/customer/vouchers" class="customer-nav-link flex items-center gap-3 px-4 py-3 rounded-2xl border border-transparent text-sm font-semibold text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-all">
            <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <rect x="3" y="5" width="18" height="14" rx="2" ry="2"/>
                <path d="M3 10h18M3 14h18"/>
                <circle cx="7" cy="12" r="1" fill="currentColor"/>
                <circle cx="17" cy="12" r="1" fill="currentColor"/>
            </svg>
            Ưu đãi
        </a>
    </nav>

    <div class="mt-auto p-4 border-t border-gray-100 space-y-2">
        <a href="${pageContext.request.contextPath}/customer/profile" class="flex items-center gap-3 px-4 py-3 rounded-2xl text-sm font-semibold text-gray-700 hover:bg-gray-50 hover:text-[#008751] transition-colors">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
            </svg>
            Thông tin cá nhân
        </a>
        <a href="${pageContext.request.contextPath}/logout" class="flex items-center gap-3 px-4 py-3 rounded-2xl text-sm font-semibold text-red-500 hover:bg-red-50 transition-colors">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
            </svg>
            Đăng xuất
        </a>
    </div>
</aside>

<script>
    (function ensureCustomerLucide() {
        function renderIcons() {
            if (window.lucide && typeof window.lucide.createIcons === "function") {
                window.lucide.createIcons();
            }
        }

        if (window.lucide) {
            renderIcons();
            return;
        }

        var existing = document.querySelector('script[data-lucide-customer="1"]');
        if (existing) {
            existing.addEventListener("load", renderIcons, { once: true });
            return;
        }

        var script = document.createElement("script");
        script.src = "https://unpkg.com/lucide@latest";
        script.defer = true;
        script.setAttribute("data-lucide-customer", "1");
        script.addEventListener("load", renderIcons, { once: true });
        document.head.appendChild(script);
    })();

    (function setupCustomerSidebar() {
        var body = document.body;
        if (body) {
            body.classList.add("customer-sidebar-layout");
        }

        var sidebar = document.getElementById("customerSidebar");
        var backdrop = document.getElementById("customerSidebarBackdrop");
        var toggle = document.getElementById("customerSidebarToggle");

        function setSidebarOpen(open) {
            if (!sidebar || !backdrop) {
                return;
            }
            sidebar.classList.toggle("open", open);
            backdrop.classList.toggle("open", open);
        }

        if (toggle) {
            toggle.addEventListener("click", function () {
                var isOpen = sidebar && sidebar.classList.contains("open");
                setSidebarOpen(!isOpen);
            });
        }

        if (backdrop) {
            backdrop.addEventListener("click", function () {
                setSidebarOpen(false);
            });
        }

        var links = document.querySelectorAll(".customer-nav-link[data-nav-key]");
        var path = window.location.pathname || "";
        var hash = window.location.hash || "";

        function activateNav(navKey) {
            links.forEach(function (link) {
                if (link.getAttribute("data-nav-key") === navKey) {
                    link.classList.add("active");
                } else {
                    link.classList.remove("active");
                }
            });
        }

        if (hash === "#locations" || path.indexOf("/customer/locations") !== -1 || path.indexOf("/customer/location-detail") !== -1) {
            activateNav("locations");
        } else if (path.indexOf("/customer/bookings") !== -1 || path.indexOf("/customer/bookingDetail") !== -1) {
            activateNav("bookings");
        } else if (path.indexOf("/customer/my-calendar") !== -1) {
            activateNav("my-calendar");
        } else if (path.indexOf("/customer/blogs") !== -1 || path.indexOf("/blog") !== -1) {
            activateNav("blogs");
        } else if (path.indexOf("/customer/vouchers") !== -1) {
            activateNav("vouchers");
        } else {
            activateNav("dashboard");
        }

        window.addEventListener("resize", function () {
            if (window.innerWidth >= 1024) {
                setSidebarOpen(false);
            }
        });
    })();
</script>
