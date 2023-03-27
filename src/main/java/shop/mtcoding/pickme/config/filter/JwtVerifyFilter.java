package shop.mtcoding.pickme.config.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import shop.mtcoding.pickme.config.auth.JwtProvider;
import shop.mtcoding.pickme.config.auth.LoginCompany;
import shop.mtcoding.pickme.config.auth.LoginUser;

public class JwtVerifyFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 요청 URL 가져오기
        String requestUri = req.getRequestURI();
        if (!requestUri.startsWith("/nc/")) {
            String prefixJwt = req.getHeader(JwtProvider.HEADER);
            String jwt = prefixJwt.replace(JwtProvider.TOKEN_PREFIX, "");
            try {
                DecodedJWT decodedJWT = JwtProvider.verify(jwt);
                int id = decodedJWT.getClaim("id").asInt();
                String role = decodedJWT.getClaim("role").asString();

                if (role.equals("user")) {
                    HttpSession session = req.getSession();
                    LoginUser loginUser = LoginUser.builder().id(id).role(role).build();
                    session.setAttribute("userPrincipal", loginUser);
                }

                if (role.equals("company")) {
                    HttpSession session = req.getSession();
                    LoginCompany loginCompany = LoginCompany.builder().id(id).role(role).build();
                    session.setAttribute("comPrincipal", loginCompany);
                }
                chain.doFilter(req, resp);
            } catch (SignatureVerificationException sve) {
                resp.setStatus(401);
                resp.setContentType("text/plain; charset=utf-8");
                resp.getWriter().println("검증 실패 : 로그인 재요청");
            } catch (TokenExpiredException tee) {
                resp.setStatus(401);
                resp.setContentType("text/plain; charset=utf-8");
                resp.getWriter().println("기간 만료 : 로그인 재요청");
            }
        } else {
            chain.doFilter(req, resp);
        }
    }
}
