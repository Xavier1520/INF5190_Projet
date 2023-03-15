import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { LoginService } from 'src/app/login/login.service';
import { FileReaderService } from '../filereader.service';
import { MessagesService } from '../messages.service';
import { WebsocketService } from '../websocket.service';

@Component({
  selector: 'app-chat-page',
  templateUrl: './chat-page.component.html',
  styleUrls: ['./chat-page.component.css'],
})
export class ChatPageComponent implements OnInit, OnDestroy {
  messages$ = this.messagesService.getMessages();
  username$ = this.loginService.getUsername();

  notifications$: Observable<'notif'> | null = null;

  messageForm = this.fb.group({
    msg: '',
  });

  currentUsername: string | null = null;

  usernameSubscription: Subscription;
  notificationSubscription: Subscription | null = null;

  constructor(
    private fb: FormBuilder,
    private messagesService: MessagesService,
    private webSocketService: WebsocketService,
    private loginService: LoginService,
    private fileReaderService: FileReaderService,
    private router: Router
  ) {
    this.usernameSubscription = this.username$.subscribe((u) => {
      this.currentUsername = u;
    });
  }

  async ngOnInit() {
    this.notifications$ = this.webSocketService.connect();
    this.notificationSubscription = this.notifications$.subscribe(
      async (n) => await this.messagesService.fetchMessages()
    );
    await this.messagesService.fetchMessages();
  }

  ngOnDestroy(): void {
    if (this.usernameSubscription) {
      this.usernameSubscription.unsubscribe();
    }
    if (this.notificationSubscription) {
      this.notificationSubscription.unsubscribe();
    }
    this.webSocketService.disconnect();
  }

  async onSendMessage(message: {msg: string, file: File | null}) {
    if (this.currentUsername) {
      let image = null;
      if (message.file) {
        image = await this.fileReaderService.readFile(message.file)
      }
      try {
        await this.messagesService.postMessage({
          text: message.msg,
          username: this.currentUsername,
          imageData: image
        });
      } catch (error) {
        await this.loginService.logout();
        this.router.navigate(['/']);
      }
    }
  }

  async onQuit() {
    this.messagesService.clear();
    await this.loginService.logout();
    this.router.navigate(['/']);
  }
}
