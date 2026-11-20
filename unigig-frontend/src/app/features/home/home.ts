import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
})
export class Home {
  protected readonly authService = inject(AuthService);

  protected readonly categories = [
    {
      name: 'Web & App Development',
      description: 'Full-stack projects, frontend interfaces, mobile apps, and APIs.',
      badge: 'Engineering',
    },
    {
      name: 'UI/UX & Graphic Design',
      description: 'Wireframes, prototypes, club posters, logos, and presentation decks.',
      badge: 'Creative',
    },
    {
      name: 'Data & AI Solutions',
      description: 'Python scripts, data cleaning, ML models, and lab analysis.',
      badge: 'Analytics',
    },
    {
      name: 'Technical Writing & Docs',
      description: 'LaTeX reports, documentation, proofreading, and research summaries.',
      badge: 'Academic',
    },
  ];

  protected readonly steps = [
    {
      step: '01',
      title: 'Join with University Email',
      description:
        'Create your verified campus account as a student freelancer or a client looking for talent.',
    },
    {
      step: '02',
      title: 'Post a Gig or Build Your Profile',
      description:
        'Clients describe their project scope and category; freelancers showcase skills and portfolios.',
    },
    {
      step: '03',
      title: 'Collaborate on Campus',
      description:
        'Connect directly with peers across faculties, deliver real-world work, and grow your portfolio.',
    },
  ];
}
