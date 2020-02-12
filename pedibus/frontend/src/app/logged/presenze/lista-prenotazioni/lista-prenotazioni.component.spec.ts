import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ListaPrenotazioniComponent} from './lista-prenotazioni.component';

describe('ListaPrenotazioniComponent', () => {
  let component: ListaPrenotazioniComponent;
  let fixture: ComponentFixture<ListaPrenotazioniComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ListaPrenotazioniComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListaPrenotazioniComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
