//package com.transcendence.security;
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JwtUtil jwtUtil;
//
//    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String authHeader = request.getHeader("Authorization");
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//            Long userId = jwtUtil.extractUserId(token);
//
//            // Set authentication in SecurityContext
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(userId, null, null);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
