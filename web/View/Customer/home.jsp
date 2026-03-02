
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Models.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>FIFA Field - Premium Pitch Booking</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
        <script src="https://unpkg.com/lucide@latest"></script>
        <style>
            body {
                font-family: 'Inter', sans-serif;
                scroll-behavior: smooth;
            }
            .scrollbar-hide::-webkit-scrollbar {
                display: none;
            }
            .scrollbar-hide {
                -ms-overflow-style: none;
                scrollbar-width: none;
            }
        </style>
    </head>
    <body class="bg-slate-50 text-slate-900">
        <!-- Navbar -->
        <nav class="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-slate-200">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div class="flex justify-between h-20 items-center">
                    <div class="flex items-center flex-shrink-0">
                        <a href="home.jsp" class="flex items-center gap-2 group">
                            <div class="bg-emerald-600 p-2 rounded-xl text-white group-hover:scale-110 transition-transform">
                                <i data-lucide="trophy" class="w-6 h-6"></i>
                            </div>
                            <span class="text-2xl font-black text-slate-900 tracking-tighter">FIFA FIELD</span>
                        </a>
                    </div>

                    <!-- Desktop Nav - Center -->
                    <div class="hidden md:flex items-center gap-8 flex-1 justify-center">
                        <a href="home.jsp" class="flex items-center gap-2 font-bold text-emerald-600 transition-colors">
                            <i data-lucide="home" class="w-[18px] h-[18px]"></i> Home
                        </a>
                        <a href="${pageContext.request.contextPath}/View/Location/location-list.jsp" class="flex items-center gap-2 font-bold text-slate-600 hover:text-emerald-600 transition-colors">
                            <i data-lucide="search" class="w-[18px] h-[18px]"></i> Browse Fields
                        </a>
                        <a href="${pageContext.request.contextPath}/View/Booking/BookingHistory.jsp" class="flex items-center gap-2 font-bold text-slate-600 hover:text-emerald-600 transition-colors">
                            <i data-lucide="calendar" class="w-[18px] h-[18px]"></i> My Bookings
                        </a>
                    </div>

                    <!-- Right - User & Profile -->
                    <div class="hidden md:flex items-center gap-4 flex-shrink-0">
                        <span class="font-bold text-slate-700">Hello, <%= currentUser.getFullName() %></span>
                        <!-- Profile dropdown -->
                        <div class="relative group">
                            <button class="text-slate-600 hover:text-emerald-600 transition-colors focus:outline-none">
                                <i data-lucide="user" class="w-6 h-6"></i>
                            </button>
                            <div class="absolute right-0 top-full mt-2 w-44 bg-white border border-slate-200 rounded-xl shadow-lg overflow-hidden opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-150 z-50">
                                <a href="${pageContext.request.contextPath}/auth/profile"
                                   class="flex items-center gap-2 px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50 hover:text-emerald-600 transition-colors">
                                    <i data-lucide="user" class="w-4 h-4"></i> Thông tin cá nhân
                                </a>
                                <a href="${pageContext.request.contextPath}/logout"
                                   class="flex items-center gap-2 px-4 py-3 text-sm font-semibold text-red-500 hover:bg-red-50 transition-colors border-t border-slate-100">
                                    <i data-lucide="log-out" class="w-4 h-4"></i> Đăng xuất
                                </a>
                            </div>
                        </div>
                    </div>

                    <!-- Mobile Menu Button -->
                    <div class="md:hidden flex items-center">
                        <button id="mobile-menu-button" class="text-slate-600 p-2 focus:outline-none">
                            <i data-lucide="menu" class="w-7 h-7"></i>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Mobile Nav Menu (Hidden by default) -->
            <div id="mobile-menu" class="hidden md:hidden bg-white border-b border-slate-200 animate-in fade-in slide-in-from-top-4 duration-200">
                <div class="px-4 pt-2 pb-6 space-y-2">
                    <a href="home.jsp" class="flex items-center gap-3 p-4 rounded-xl font-bold bg-emerald-50 text-emerald-600">
                        <i data-lucide="home"></i> Home
                    </a>
                    <a href="browse.jsp" class="flex items-center gap-3 p-4 rounded-xl font-bold text-slate-600">
                        <i data-lucide="search"></i> Browse Fields
                    </a>
                    <a href="bookings.jsp" class="flex items-center gap-3 p-4 rounded-xl font-bold text-slate-600">
                        <i data-lucide="calendar"></i> My Bookings
                    </a>
                    <div class="pt-2 flex items-center gap-4 px-4 border-t border-slate-100">
                        <i data-lucide="user" class="w-5 h-5 text-slate-500"></i>
                        <span class="font-bold text-slate-700 flex-1"><%= currentUser.getFullName() %></span>
                        <a href="${pageContext.request.contextPath}/auth/profile" class="text-sm font-semibold text-emerald-600 hover:underline">Profile</a>
                        <a href="${pageContext.request.contextPath}/logout" class="text-sm font-semibold text-red-500 hover:underline">Logout</a>
                    </div>
                </div>
            </div>
        </nav>

        <!-- Hero Section -->
        <section class="relative h-[85vh] flex items-center overflow-hidden">
            <div class="absolute inset-0">
                <img src="https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&q=80" alt="Football Pitch" class="w-full h-full object-cover">
                <div class="absolute inset-0 bg-gradient-to-r from-slate-950 via-slate-900/80 to-transparent"></div>
            </div>

            <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-20">
                <div class="max-w-2xl">
                    <div class="inline-flex items-center gap-2 bg-emerald-600/20 backdrop-blur-md border border-emerald-500/30 px-4 py-2 rounded-full text-emerald-400 font-bold text-sm mb-8">
                        <i data-lucide="zap" class="w-4 h-4 text-emerald-400"></i>
                        <span class="uppercase tracking-widest">THE ULTIMATE PITCH BOOKING EXPERIENCE</span>
                    </div>
                    <h1 class="text-6xl md:text-8xl font-black text-white leading-none tracking-tighter mb-8 animate-in fade-in slide-in-from-left-6 duration-700">
                        FIND YOUR <span class="text-emerald-500">ARENA.</span>
                    </h1>
                    <p class="text-xl text-slate-300 font-medium mb-12 max-w-lg leading-relaxed">
                        Join 10,000+ players booking top-rated premium football pitches across the city. Professional quality, instant booking.
                    </p>
                    <div class="flex flex-col sm:flex-row gap-4">
                        <a href="<%=request.getContextPath()%>/booking" class="flex items-center justify-center gap-2 bg-emerald-600 text-white px-10 py-5 rounded-2xl font-black text-lg hover:bg-emerald-700 transition-all shadow-xl shadow-emerald-600/20 active:scale-[0.98]">
                            BOOK A PITCH
                            <i data-lucide="chevron-right"></i>
                        </a>
                        <button href= "/View/Location/location-detail.jsp" class="flex items-center justify-center gap-2 bg-white/10 backdrop-blur-md border border-white/20 text-white px-10 py-5 rounded-2xl font-black text-lg hover:bg-white/20 transition-all">
                            OUR LOCATIONS
                        </button>
                    </div>
                </div>
            </div>
        </section>

        <!-- Stats Section -->
        <section class="bg-white py-12 border-b border-slate-200">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div class="grid grid-cols-2 md:grid-cols-4 gap-8">
                    <div class="flex flex-col items-center md:items-start text-center md:text-left gap-2">
                        <i data-lucide="shield" class="text-emerald-600 w-7 h-7"></i>
                        <div class="font-black text-slate-900 uppercase tracking-tighter">Secure Booking</div>
                        <div class="text-xs text-slate-500 font-bold uppercase">Guaranteed Slots</div>
                    </div>
                    <div class="flex flex-col items-center md:items-start text-center md:text-left gap-2">
                        <i data-lucide="clock" class="text-emerald-600 w-7 h-7"></i>
                        <div class="font-black text-slate-900 uppercase tracking-tighter">24/7 Access</div>
                        <div class="text-xs text-slate-500 font-bold uppercase">Real-time Updates</div>
                    </div>
                    <div class="flex flex-col items-center md:items-start text-center md:text-left gap-2">
                        <i data-lucide="award" class="text-emerald-600 w-7 h-7"></i>
                        <div class="font-black text-slate-900 uppercase tracking-tighter">Premium Turf</div>
                        <div class="text-xs text-slate-500 font-bold uppercase">Quality Inspected</div>
                    </div>
                    <div class="flex flex-col items-center md:items-start text-center md:text-left gap-2">
                        <i data-lucide="users" class="text-emerald-600 w-7 h-7"></i>
                        <div class="font-black text-slate-900 uppercase tracking-tighter">10k+ Players</div>
                        <div class="text-xs text-slate-500 font-bold uppercase">Active Community</div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Featured Fields -->
        <section class="py-24 bg-slate-50">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div class="flex justify-between items-end mb-12">
                    <div>
                        <h2 class="text-4xl font-black text-slate-900 tracking-tighter mb-4 uppercase">Featured Arenas</h2>
                        <p class="text-slate-500 font-bold uppercase tracking-widest text-sm">Most booked this week</p>
                    </div>
                    <a href="browse.jsp" class="hidden sm:flex items-center gap-2 text-emerald-600 font-black hover:translate-x-1 transition-all">
                        VIEW ALL FIELDS
                        <i data-lucide="chevron-right"></i>
                    </a>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                    <!-- Arena Card 1 -->
                    <div class="group bg-white rounded-3xl overflow-hidden border border-slate-200 hover:border-emerald-500 hover:shadow-2xl hover:shadow-emerald-900/5 transition-all duration-300">
                        <div class="relative h-56 overflow-hidden">
                            <img src="https://picsum.photos/seed/stadium1/800/600" alt="Bernabéu" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                            <div class="absolute top-4 left-4 bg-white/90 backdrop-blur-md px-3 py-1.5 rounded-full flex items-center gap-1.5">
                                <i data-lucide="star" class="w-4 h-4 text-amber-500 fill-amber-500"></i>
                                <span class="text-sm font-bold text-slate-900">4.9</span>
                                <span class="text-xs text-slate-500">(128)</span>
                            </div>
                            <div class="absolute bottom-4 right-4 bg-emerald-600 text-white px-3 py-1.5 rounded-full font-bold text-sm">
                                $45/hr
                            </div>
                        </div>
                        <div class="p-6">
                            <h3 class="text-xl font-black text-slate-900 mb-2 leading-tight uppercase">Bernabéu Stadium Turf</h3>
                            <div class="flex items-center gap-2 text-slate-500 mb-4">
                                <i data-lucide="map-pin" class="w-4 h-4"></i>
                                <span class="text-sm font-medium">District 1, Ho Chi Minh City</span>
                            </div>
                            <div class="flex flex-wrap gap-2 mb-6">
                                <div class="flex items-center gap-1.5 bg-slate-50 px-2.5 py-1 rounded-lg border border-slate-100">
                                    <i data-lucide="users" class="w-3.5 h-3.5 text-emerald-600"></i>
                                    <span class="text-xs font-bold text-slate-600 uppercase tracking-wider">5-a-side</span>
                                </div>
                            </div>
                            <a href="booking.jsp?id=1" class="block w-full text-center bg-slate-900 text-white py-4 rounded-2xl font-black hover:bg-emerald-600 transition-all shadow-lg active:scale-95">
                                BOOK NOW
                            </a>
                        </div>
                    </div>

                    <!-- Arena Card 2 -->
                    <div class="group bg-white rounded-3xl overflow-hidden border border-slate-200 hover:border-emerald-500 hover:shadow-2xl hover:shadow-emerald-900/5 transition-all duration-300">
                        <div class="relative h-56 overflow-hidden">
                            <img src="https://picsum.photos/seed/stadium2/800/600" alt="Old Trafford" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                            <div class="absolute top-4 left-4 bg-white/90 backdrop-blur-md px-3 py-1.5 rounded-full flex items-center gap-1.5">
                                <i data-lucide="star" class="w-4 h-4 text-amber-500 fill-amber-500"></i>
                                <span class="text-sm font-bold text-slate-900">4.8</span>
                                <span class="text-xs text-slate-500">(85)</span>
                            </div>
                            <div class="absolute bottom-4 right-4 bg-emerald-600 text-white px-3 py-1.5 rounded-full font-bold text-sm">
                                $60/hr
                            </div>
                        </div>
                        <div class="p-6">
                            <h3 class="text-xl font-black text-slate-900 mb-2 leading-tight uppercase">Old Trafford Arena</h3>
                            <div class="flex items-center gap-2 text-slate-500 mb-4">
                                <i data-lucide="map-pin" class="w-4 h-4"></i>
                                <span class="text-sm font-medium">District 7, Ho Chi Minh City</span>
                            </div>
                            <div class="flex flex-wrap gap-2 mb-6">
                                <div class="flex items-center gap-1.5 bg-slate-50 px-2.5 py-1 rounded-lg border border-slate-100">
                                    <i data-lucide="users" class="w-3.5 h-3.5 text-emerald-600"></i>
                                    <span class="text-xs font-bold text-slate-600 uppercase tracking-wider">7-a-side</span>
                                </div>
                            </div>
                            <a href="booking.jsp?id=2" class="block w-full text-center bg-slate-900 text-white py-4 rounded-2xl font-black hover:bg-emerald-600 transition-all shadow-lg active:scale-95">
                                BOOK NOW
                            </a>
                        </div>
                    </div>

                    <!-- Arena Card 3 -->
                    <div class="group bg-white rounded-3xl overflow-hidden border border-slate-200 hover:border-emerald-500 hover:shadow-2xl hover:shadow-emerald-900/5 transition-all duration-300">
                        <div class="relative h-56 overflow-hidden">
                            <img src="https://picsum.photos/seed/stadium3/800/600" alt="Anfield" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                            <div class="absolute top-4 left-4 bg-white/90 backdrop-blur-md px-3 py-1.5 rounded-full flex items-center gap-1.5">
                                <i data-lucide="star" class="w-4 h-4 text-amber-500 fill-amber-500"></i>
                                <span class="text-sm font-bold text-slate-900">5.0</span>
                                <span class="text-xs text-slate-500">(240)</span>
                            </div>
                            <div class="absolute bottom-4 right-4 bg-emerald-600 text-white px-3 py-1.5 rounded-full font-bold text-sm">
                                $80/hr
                            </div>
                        </div>
                        <div class="p-6">
                            <h3 class="text-xl font-black text-slate-900 mb-2 leading-tight uppercase">Anfield Premium Field</h3>
                            <div class="flex items-center gap-2 text-slate-500 mb-4">
                                <i data-lucide="map-pin" class="w-4 h-4"></i>
                                <span class="text-sm font-medium">District 2, Ho Chi Minh City</span>
                            </div>
                            <div class="flex flex-wrap gap-2 mb-6">
                                <div class="flex items-center gap-1.5 bg-slate-50 px-2.5 py-1 rounded-lg border border-slate-100">
                                    <i data-lucide="users" class="w-3.5 h-3.5 text-emerald-600"></i>
                                    <span class="text-xs font-bold text-slate-600 uppercase tracking-wider">11-a-side</span>
                                </div>
                            </div>
                            <a href="booking.jsp?id=3" class="block w-full text-center bg-slate-900 text-white py-4 rounded-2xl font-black hover:bg-emerald-600 transition-all shadow-lg active:scale-95">
                                BOOK NOW
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Footer -->
        <footer class="bg-slate-900 text-white py-20 border-t border-slate-800">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
                <div class="flex items-center justify-center gap-2 mb-8">
                    <div class="bg-emerald-600 p-2 rounded-xl">
                        <i data-lucide="trophy" class="w-8 h-8 text-white"></i>
                    </div>
                    <span class="text-3xl font-black tracking-tighter">FIFA FIELD</span>
                </div>
                <p class="text-slate-400 font-medium mb-12 max-w-md mx-auto">Premium football pitch booking made simple for every player in the city.</p>
                <div class="flex justify-center gap-8 mb-16">
                    <a href="#" class="text-slate-400 hover:text-white transition-colors"><i data-lucide="facebook"></i></a>
                    <a href="#" class="text-slate-400 hover:text-white transition-colors"><i data-lucide="instagram"></i></a>
                    <a href="#" class="text-slate-400 hover:text-white transition-colors"><i data-lucide="twitter"></i></a>
                </div>
                <div class="pt-12 border-t border-slate-800 text-xs text-slate-500 font-bold uppercase tracking-[0.2em]">
                    &copy; 2026 FIFA FIELD. DEVELOPED FOR CHAMPIONS.
                </div>
            </div>
        </footer>

        <script>
            // Initialize Icons
            lucide.createIcons();

            // Mobile Menu Script
            const menuBtn = document.getElementById('mobile-menu-button');
            const mobileMenu = document.getElementById('mobile-menu');

            menuBtn.addEventListener('click', () => {
                mobileMenu.classList.toggle('hidden');
            });
        </script>
    </body>
</html>
