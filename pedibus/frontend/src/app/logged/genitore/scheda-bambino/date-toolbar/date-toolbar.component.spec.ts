import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DateToolbarComponent } from './date-toolbar.component';

describe('DateToolbarComponent', () => {
  let component: DateToolbarComponent;
  let fixture: ComponentFixture<DateToolbarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DateToolbarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DateToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
