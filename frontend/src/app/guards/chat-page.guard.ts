import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { LoginService } from '../login/login.service';

@Injectable({
  providedIn: 'root'
})
export class ChatPageGuard implements CanActivate {
  constructor(private router: Router, private loginService: LoginService) {}
  
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.loginService.getToken()) {
      return true;
    }
    this.router.navigate(['/']);
    return false;
  }
  
}
