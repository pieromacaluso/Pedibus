import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PresenceDialogComponent} from './presence-dialog.component';

describe('PresenceDialogComponent', () => {
  let component: PresenceDialogComponent;
  let fixture: ComponentFixture<PresenceDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PresenceDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PresenceDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
