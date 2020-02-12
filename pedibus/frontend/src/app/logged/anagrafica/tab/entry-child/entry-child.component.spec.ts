import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {EntryChildComponent} from './entry-child.component';

describe('EntryChildComponent', () => {
  let component: EntryChildComponent;
  let fixture: ComponentFixture<EntryChildComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [EntryChildComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntryChildComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
