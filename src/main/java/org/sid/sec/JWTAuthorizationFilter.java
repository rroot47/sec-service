package org.sid.sec;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

public class JWTAuthorizationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Authorizations");
        response.addHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin, Access-Control-Allow-Headers");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH");

        if (request.getMethod().equals("OPTIONS")){
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }else if (request.getRequestURI().equals("/login")){
            filterChain.doFilter(request,response);
            return;
        }
        else {

            String jwt = request.getHeader(Securityparams.HEADER_NAME);
            System.out.println("Token :" +jwt);

            if (jwt==null || !jwt.startsWith(Securityparams.HEADER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }
            System.out.println("iciicccccccc");
            JWTVerifier jwtVerifier=JWT.require(Algorithm.HMAC256(Securityparams.SECRET)).build();
            String jwtToken=jwt.substring(Securityparams.HEADER_PREFIX.length());
            DecodedJWT decodedJWT=jwtVerifier.verify(jwtToken);
            System.out.println("JWT:"+jwtToken);
            String username = decodedJWT.getSubject();//pour recuperer les usernames
           // List<String> role=decodedJWT.getClaims().get("role").asList(String.class);
            List<String> roles=decodedJWT.getClaims().get("rolesliste").asList(String.class);
            System.out.println("username:" +username);
            System.out.println("roles:" +roles);
            Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            roles.forEach(rn -> {
                grantedAuthorities.add(new SimpleGrantedAuthority(rn));
            });
            //pour authentifier les users
            UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(username,null,grantedAuthorities);
            SecurityContextHolder.getContext().setAuthentication(userToken);
            filterChain.doFilter(request, response);
        }
    }
}
