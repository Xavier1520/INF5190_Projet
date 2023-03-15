import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {
  private ws: WebSocket | null = null;

  constructor() {}

  public connect() {
    this.ws = new WebSocket(`${environment.wsServer}/notifications`);
    const events = new Subject<'notif'>();

    this.ws.onmessage = () => events.next('notif');
    this.ws.onclose = () => events.complete();
    this.ws.onerror = () => events.error('error');

    return events.asObservable();
  }

  public disconnect() {
    this.ws?.close();
    this.ws = null;
  }
}
