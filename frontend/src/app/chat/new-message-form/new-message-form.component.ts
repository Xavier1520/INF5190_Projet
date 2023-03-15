import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-new-message-form',
  templateUrl: './new-message-form.component.html',
  styleUrls: ['./new-message-form.component.css'],
})
export class NewMessageForm implements OnInit {
  messageForm = this.fb.group({
    msg: '',
  });

  file: File | null = null;

  @Output()
  sendMessage = new EventEmitter<{msg: string, file: File | null}>();
  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {}

  fileChanged(event: any) {
    this.file = event.target.files[0];
  }

  onSend() {
    if (this.messageForm.valid && this.messageForm.value.msg) {
      this.sendMessage.emit({msg: this.messageForm.value.msg, file: this.file});
      this.messageForm.reset();
    }
  }
}
