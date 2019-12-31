import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DialogAnagraficaComponent } from './dialog-anagrafica.component';

describe('DialogAnagraficaComponent', () => {
  let component: DialogAnagraficaComponent;
  let fixture: ComponentFixture<DialogAnagraficaComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DialogAnagraficaComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DialogAnagraficaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
