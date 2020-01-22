import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GuideEntryComponent } from './guide-entry.component';

describe('GuideEntryComponent', () => {
  let component: GuideEntryComponent;
  let fixture: ComponentFixture<GuideEntryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GuideEntryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GuideEntryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
