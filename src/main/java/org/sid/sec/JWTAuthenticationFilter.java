package org.sid.sec;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sid.entities.AppUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.lang.String;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
          AppUser  appUser = new ObjectMapper().readValue(request.getInputStream(), AppUser.class);
            return  authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(appUser.getUsername(), appUser.getPassword()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problem in Request Content");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        //super.successfulAuthentication(request, response, chain, authResult);

        User springUser=(User) authResult.getPrincipal();
        List<String> rolesliste=new ArrayList<>();
        authResult.getAuthorities().forEach(a->{
            rolesliste.add(a.getAuthority());
        });
        //generation de Token JWT
        String jwt=JWT.create()
                .withIssuer(request.getRequestURI()) //nom de l'autorité l'application qui as gener le token
                .withSubject(springUser.getUsername()) //nom utilisateur
                .withArrayClaim("rolesliste", rolesliste.toArray(new String[rolesliste.size()])) //le role sous forme d'un tableau
                .withExpiresAt(new Date(System.currentTimeMillis()+Securityparams.EXPIRATION)) //date d'expiration
                .sign(Algorithm.HMAC256(Securityparams.SECRET));//pour la signature
        response.addHeader(Securityparams.HEADER_NAME, jwt);
    }
}
