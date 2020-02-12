import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DialogPrenotazioneComponent} from './dialog-prenotazione.component';

describe('DialogPrenotazioneComponent', () => {
  let component: DialogPrenotazioneComponent;
  let fixture: ComponentFixture<DialogPrenotazioneComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DialogPrenotazioneComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DialogPrenotazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
