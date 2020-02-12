import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AggiuntaDispComponent} from './aggiunta-disp.component';

describe('AggiuntaDispComponent', () => {
  let component: AggiuntaDispComponent;
  let fixture: ComponentFixture<AggiuntaDispComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AggiuntaDispComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AggiuntaDispComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
