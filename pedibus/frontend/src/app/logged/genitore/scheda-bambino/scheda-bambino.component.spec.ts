import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SchedaBambinoComponent } from './scheda-bambino.component';

describe('SchedaBambinoComponent', () => {
  let component: SchedaBambinoComponent;
  let fixture: ComponentFixture<SchedaBambinoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SchedaBambinoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SchedaBambinoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
