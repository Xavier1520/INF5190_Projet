import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from '../login.service';

@Component({
  selector: 'app-login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.css'],
})
export class LoginPageComponent implements OnInit {
  error: string | null = null;
  
  constructor(private router: Router, private loginService: LoginService) {}

  ngOnInit(): void {}

  async onLogin(login: { username: string; password: string }) {
    try {
      await this.loginService.login(login);
      this.router.navigate(['/chat']);
    } catch (error) {
      if (error instanceof HttpErrorResponse) {
        this.error = "Mot de passe invalide"
      } else {
        this.error = "Probleme de connexion"
      }
    }
  }
}
