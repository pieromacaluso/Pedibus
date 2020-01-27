import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {PrenotazioneRequest} from '../../../line-details';
import {SyncService} from '../../../presenze/sync.service';
import {BehaviorSubject, Observable} from 'rxjs';
import {Notifica} from '../../../notifiche/dtos';

@Component({
  selector: 'app-date-toolbar',
  templateUrl: './date-toolbar.component.html',
  styleUrls: ['./date-toolbar.component.scss']
})
export class DateToolbarComponent implements OnChanges {

  @Output() dateEmitter = new EventEmitter();
  date: Date = new Date();


  constructor(private syncService: SyncService) {
    this.dateEmitter.emit(this.date);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.date.currentValue) {
      this.date = new Date();
      this.emitRequest();
    }
  }

  emitRequest() {
    this.dateEmitter.emit(this.date);
  }

  modifyDate(days: number) {
    const nextDate = new Date(this.date);
    nextDate.setDate(nextDate.getDate() + days);
    this.date = nextDate;
    this.emitRequest();
  }

}
