import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { TestHelper } from 'src/app/tests/test-helper';

import { LoginFormComponent } from './login-form.component';

describe('LoginFormComponent', () => {
  let component: LoginFormComponent;
  let fixture: ComponentFixture<LoginFormComponent>;
  let helper: TestHelper<LoginFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginFormComponent],
      imports: [ReactiveFormsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginFormComponent);
    component = fixture.componentInstance;
    helper = new TestHelper(fixture);
    fixture.detectChanges();
  });

  it('should emit username and password', () => {
    let username: string;
    let password: string;
  
    // On s'abonne à l'EventEmitter pour recevoir les valeurs émises.
    component.login.subscribe((event) => {
      username = event.username;
      password = event.password;
    });
  
    // On rempli le formulaire.
    const usernameInput = helper.getInput('username-input');
    helper.writeInInput(usernameInput, 'username');
    const passwordInput = helper.getInput('password-input');
    helper.writeInInput(passwordInput, 'pwd');

    // On force la detection de changement.
    fixture.detectChanges();

    // On simule un clique sur le boutton.
    const button = helper.getButton('login-button');
    button.click();

    expect(username!).toBe('username');
    expect(password!).toBe('pwd');
    expect(component.loginForm.valid).toBe(true);
  });

  it('should not allow empty username', () => {
    let username: string;
    let password: string;
  
    // On s'abonne à l'EventEmitter pour recevoir les valeurs émises.
    component.login.subscribe((event) => {
      username = event.username;
      password = event.password;
    });

    // On rempli le formulaire.
    const passwordInput = helper.getInput('password-input');
    helper.writeInInput(passwordInput, 'pwd');

    // On simule un clique sur le boutton.
    const button = helper.getButton('login-button');
    button.click();

    expect(username!).toBeUndefined();
    expect(password!).toBeUndefined();
    expect(component.loginForm.valid).toBe(false);
  });

  it('should not allow empty password', () => {
    let username: string;
    let password: string;
  
    // On s'abonne à l'EventEmitter pour recevoir les valeurs émises.
    component.login.subscribe((event) => {
      username = event.username;
      password = event.password;
    });
    
    // On rempli le formulaire.
    const usernameInput = helper.getInput('username-input');
    helper.writeInInput(usernameInput, 'username');

    // On simule un clique sur le boutton.
    const button = helper.getButton('login-button');
    button.click();

    expect(username!).toBeUndefined();
    expect(password!).toBeUndefined();
    expect(component.loginForm.valid).toBe(false);
  });

  it('should not allow empty username and password', () => {
    let username: string;
    let password: string;
  
    // On s'abonne à l'EventEmitter pour recevoir les valeurs émises.
    component.login.subscribe((event) => {
      username = event.username;
      password = event.password;
    });

    // On simule un clique sur le boutton.
    const button = helper.getButton('login-button');
    button.click();

    expect(username!).toBeUndefined();
    expect(password!).toBeUndefined();
    expect(component.loginForm.valid).toBe(false);
  });
});
