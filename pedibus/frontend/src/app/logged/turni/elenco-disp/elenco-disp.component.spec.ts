import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ElencoDispComponent } from './elenco-disp.component';

describe('ElencoDispComponent', () => {
  let component: ElencoDispComponent;
  let fixture: ComponentFixture<ElencoDispComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ElencoDispComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ElencoDispComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
